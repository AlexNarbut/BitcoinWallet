package com.test.transaction.data.builder

import com.test.common.response.Response
import com.test.commonextens.response.asGeneralError
import com.test.commonextens.response.asResponse
import com.test.commonextens.response.getValueIfSuccess
import com.test.commonextens.response.safeRun
import org.bitcoinj.base.AddressParser
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Coin
import org.bitcoinj.base.ScriptType
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Context
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.TransactionWitness
import org.bitcoinj.crypto.DumpedPrivateKey
import org.bitcoinj.script.ScriptBuilder
import test.transaction.api.builder.TransactionBuilder
import test.transaction.api.exception.TransactionNotEnoughCoinsException
import test.transaction.api.exception.TransactionUtxoNotFoundException
import test.transaction.api.model.TransactionInfo
import test.transaction.api.model.TransactionStatus
import test.transaction.api.model.UtxoSendParams
import test.transaction.api.repository.TransactionRepository
import test.transaction.api.repository.param.GetTransactionsRequest

class BitcoinjSignetTransactionBuilder(
    private val transactionRepository: TransactionRepository
) : TransactionBuilder {

    override suspend fun createSendTransactionHex(
        walletAddress: String,
        primaryKey: String,
        destinationAddress: String,
        amount: Long,
        feeAmount: Long
    ): Response<String> {
        val transactionInfoResult = transactionRepository.getAddressTransaction(
            GetTransactionsRequest(walletAddress)
        )
        if (transactionInfoResult is Response.Error) return transactionInfoResult

        val utxoParams = findSuitableUtxo(
            transactionInfoResult.getValueIfSuccess() ?: emptyList(),
            amount, feeAmount
        ) ?: return TransactionUtxoNotFoundException(message =  "Utxo Not Found").asGeneralError()

        return prepareTransaction(
            utxoParams,
            primaryKey,
            destinationAddress,
            amount,
            feeAmount
        )
    }

    private fun findSuitableUtxo(
        transactions: List<TransactionInfo>,
        amount: Long,
        feeAmount: Long
    ): UtxoSendParams? {
        for (tx in transactions) {
            if (tx.transactionStatus == TransactionStatus.CONFIRMED) {
                tx.vOutput.forEachIndexed { index, vout ->
                    if (vout.value >= (amount + feeAmount + DUST_THRESHOLD)) {
                        // Check that this output has not been used as an input (UTXO)
                        val isUsed = transactions.any { transaction ->
                            transaction.vInput.any { vin -> vin.txId == tx.id && vin.vOut == index }
                        }

                        // If UTXO was not used, return it
                        if (!isUsed) {
                            return UtxoSendParams(tx.id, index.toLong(), vout.value)
                        }
                    }
                }
            }
        }
        return null
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun prepareTransaction(
        utxoSendParams: UtxoSendParams,
        primaryKey: String,
        destinationAddress: String,
        amount: Long,
        feeAmount: Long,
    ): Response<String> = safeRun {
        Context.propagate(Context())

        // Base network settings
        val scriptType = ScriptType.P2WPKH
        val network = BitcoinNetwork.SIGNET

        // Get dumped private key from P2WPKH
        val cleanKey = primaryKey.substringAfter(':')
        val key = DumpedPrivateKey.fromBase58(network, cleanKey).key

        val addressParser = AddressParser.getDefault()
        val toAddress = addressParser.parseAddress(destinationAddress)

        val sendAmount = Coin.valueOf(amount)

        // Total sum of outputs (UTXO)
        val totalInput = Coin.valueOf(utxoSendParams.valueInSat)
        // Commission to miners.
        // Min 220 satoshi, otherwise you will get "min relay fee not met, 1 < 220" from backend
        val fee = Coin.valueOf(feeAmount)

        // We check if there are enough funds to send, taking into account the commission
        if (totalInput.subtract(sendAmount) < fee) {
            return TransactionNotEnoughCoinsException(message = "Not enough funds to send transaction with fee").asGeneralError()
        }

        val transaction = Transaction()
        // Add output - recipient address and amount
        transaction.addOutput(sendAmount, toAddress)

        // Calculate change (if any)
        val change = totalInput.subtract(sendAmount).subtract(fee)
        if (change.isPositive) {
            // Important: add change to the sender's address
            transaction.addOutput(change, key.toAddress(scriptType, network))
        }

        // UTXO - transaction from which we spend
        val utxo = Sha256Hash.wrap(utxoSendParams.txId)
        val outPoint = TransactionOutPoint(utxoSendParams.vOutIndex, utxo)
        val input =
            TransactionInput(
                transaction,
                byteArrayOf(),
                outPoint,
                Coin.valueOf(utxoSendParams.valueInSat)
            )

        // Add input. Important: need to add it after adding of outputs
        transaction.addInput(input)

        // Get scriptPubKey for signing from previous output (UTXO)
        val scriptCode = ScriptBuilder.createP2PKHOutputScript(key.pubKeyHash)

        // Sign inputs after adding of all outputs
        for (i in 0 until transaction.inputs.size) {
            val txIn = transaction.getInput(i.toLong())
            val signature = transaction.calculateWitnessSignature(
                i,
                key,
                scriptCode,
                Coin.valueOf(utxoSendParams.valueInSat),
                Transaction.SigHash.ALL,
                false
            )
            txIn.witness = TransactionWitness.of(listOf(signature.encodeToBitcoin(), key.pubKey))
        }

        // Convert the transaction to HEX for sending.
        // We need to send only HEX as a plain text.
        return transaction.serialize().toHexString().asResponse()
    }

    companion object {
        private const val DUST_THRESHOLD = 300
    }
}
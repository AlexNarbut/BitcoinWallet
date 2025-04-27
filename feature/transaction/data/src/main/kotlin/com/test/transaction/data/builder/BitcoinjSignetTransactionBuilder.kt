package com.test.transaction.data.builder

import com.test.common.response.Response
import com.test.commonextens.response.asGeneralError
import com.test.commonextens.response.asResponse
import com.test.commonextens.response.getValueIfSuccess
import com.test.commonextens.response.mapValueIfSuccess
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

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun createSendTransactionHex(
        walletAddress: String,
        primaryKey: String,
        destinationAddress: String,
        amount: Long,
        neededFeeAmount: Long?
    ): Response<String> {
        val transactionInfoResult = transactionRepository.getAddressTransaction(
            GetTransactionsRequest(walletAddress)
        )
        if (transactionInfoResult is Response.Error) return transactionInfoResult


        val feeResponse = calculateFeeIfNeeded(
            neededFeeAmount,
            transactionInfoResult.getValueIfSuccess() ?: emptyList(),
            primaryKey,
            destinationAddress,
            amount,
        )

        if (feeResponse is Response.Error) return feeResponse

        return prepareTransaction(
            transactionInfoResult.getValueIfSuccess() ?: emptyList(),
            primaryKey,
            destinationAddress,
            amount,
            feeResponse.getValueIfSuccess() ?: DEFAULT_FEE
        ).mapValueIfSuccess { it.toHexString() }
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

    private fun prepareTransaction(
        walletAddressTransactions: List<TransactionInfo>,
        primaryKey: String,
        destinationAddress: String,
        amount: Long,
        feeAmount: Long,
    ): Response<ByteArray> = safeRun {
        val utxoParams = findSuitableUtxo(
            walletAddressTransactions,
            amount,
            feeAmount
        ) ?: return TransactionUtxoNotFoundException(message = "Utxo Not Found").asGeneralError()


        Context.propagate(Context())
        // Get dumped private key from P2WPKH
        val cleanKey = primaryKey.substringAfter(':')
        val key = DumpedPrivateKey.fromBase58(NETWORK, cleanKey).key

        val addressParser = AddressParser.getDefault()
        val toAddress = addressParser.parseAddress(destinationAddress)

        val sendAmount = Coin.valueOf(amount)

        // Total sum of outputs (UTXO)
        val totalInput = Coin.valueOf(utxoParams.valueInSat)
        // Commission to miners.
        // Min 220 satoshi, otherwise you will get "min relay fee not met, 1 < 220" from backend
        val fee = Coin.valueOf(feeAmount)

        // We check if there are enough funds to send, taking into account the commission
        if (totalInput.subtract(sendAmount) < fee) {
            return TransactionNotEnoughCoinsException(
                inputCoinsInSat = totalInput.value,
                neededCoinsWithFeeInSat = sendAmount.value + fee.value,
                message = "Not enough funds to send transaction with fee"
            ).asGeneralError()
        }

        val transaction = Transaction()
        // Add output - recipient address and amount
        transaction.addOutput(sendAmount, toAddress)

        // Calculate change (if any)
        val change = totalInput.subtract(sendAmount).subtract(fee)
        if (change.isPositive) {
            // Important: add change to the sender's address
            transaction.addOutput(change, key.toAddress(SCRIPT_TYPE, NETWORK))
        }

        // UTXO - transaction from which we spend
        val utxo = Sha256Hash.wrap(utxoParams.txId)
        val outPoint = TransactionOutPoint(utxoParams.vOutIndex, utxo)
        val input =
            TransactionInput(
                transaction,
                byteArrayOf(),
                outPoint,
                Coin.valueOf(utxoParams.valueInSat)
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
                Coin.valueOf(utxoParams.valueInSat),
                Transaction.SigHash.ALL,
                false
            )
            txIn.witness = TransactionWitness.of(listOf(signature.encodeToBitcoin(), key.pubKey))
        }

        // Convert the transaction to HEX for sending.
        // We need to send only HEX as a plain text.
        return transaction.serialize().asResponse()
    }

    private suspend fun calculateFeeIfNeeded(
        neededFeeAmount: Long?,
        transactions: List<TransactionInfo>,
        primaryKey: String,
        destinationAddress: String,
        amount: Long,
    ): Response<Long> {
        if (neededFeeAmount != null) return neededFeeAmount.asResponse()

        val defaultTransactionResponse = prepareTransaction(
            transactions,
            primaryKey,
            destinationAddress,
            amount,
            DEFAULT_FEE
        )

        if (defaultTransactionResponse is Response.Error) return defaultTransactionResponse

        val feeRateResponse = transactionRepository.getRecommendedFee()
        if (feeRateResponse is Response.Error) return feeRateResponse

        val byteSize = (defaultTransactionResponse.getValueIfSuccess()?.size ?: 0)
        val feeRate = feeRateResponse.getValueIfSuccess()?.economyFeeInSat?:0
        return (byteSize * feeRate).asResponse()
    }


    companion object {
        private const val DUST_THRESHOLD = 300

        private const val DEFAULT_FEE = 1L

        // Base network settings
        val SCRIPT_TYPE = ScriptType.P2WPKH
        val NETWORK = BitcoinNetwork.SIGNET
    }
}
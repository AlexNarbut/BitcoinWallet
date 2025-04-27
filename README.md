# Bitcoin Signet Wallet Android App

A Wallet application for Android for the Bitcoin (Signet) network.

Stack: Kotlin, Coroutines,Hilt, Bitcoinj, Mempool API
UI stack: Compose, MVVM, Compose Navigation

## Features:
- **Current Address, Wallet balance View**
- **Send Transactions with calculated fee**
- **Transaction History**

## Setup Instructions
1. **Clone the Repository**.
2. **Open in Android Studio**:  
   Launch Android Studio.  
   Click on **File > Open** and navigate to the cloned repository.
3. **Prepare wallet**
   Create wallet in Bitcoin Signet (for example, Electrum), find address's information: addresses and primary keys. 
4. **Add Required wallet information to Project**:
   Copy and past addresses and primary keys in  the following files in the `assets` folder of the project:
    - **keys.txt**: Contains your private key in WIF format. Warning: This is sensitive information. Handle it securely and never share it publicly.
    - **addresses.txt**: Contains a list of your Bitcoin addresses, one per line.

## Important Notes
  **Private Key**: The `keys.txt` file contains your private key. Ensure it is stored securely and is not exposed to unauthorized individuals.
  For best practices you need get wallet information from server (Https + Ssl pinning) and save in Android Keystore





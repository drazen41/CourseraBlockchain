import java.awt.List;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;



public class TxHandler_Fails {

   public UTXOPool utxoPool ;
	/**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler_Fails(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	this.utxoPool = new UTXOPool(utxoPool) ;    	
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	boolean ok = true;   	   	
    	ArrayList<Transaction.Input> inputs = tx.getInputs();
    	ArrayList<Transaction.Output > outputs = tx.getOutputs();
    	int i = 0;
    	double inputSum = 0;
    	double outputSum = 0;
    	
    	ArrayList<Transaction.Output> usedOutputs = new ArrayList<Transaction.Output>();
    	if (inputs != null) {
    		for (Transaction.Input input  :  inputs) {
    			try {
//    				if (input.prevTxHash.hashCode() == 0) continue;
    				UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        			Transaction.Output output = this.utxoPool.getTxOutput(utxo);
        			if (usedOutputs.contains(output )) {
        				return false;
        			}
        			else {
        				if (output == null) return true; //root transaction
        				usedOutputs.add(output);
        			}
        			inputSum += output.value ;
        			
        			Transaction.Output output2 = tx.getOutput(input.outputIndex );
        			
        			
        			PublicKey publicKey = output.address;
        			byte[] message = tx.getRawDataToSign(i);
        			byte[] signature = input.signature;
        			try {
						boolean verify = Crypto.verifySignature(publicKey, message, signature);
						if (!verify) return false;
//						System.out.println(input.signature.toString() + "-> sig, " + i + "->brojac");
					} catch (Exception e) {
						// TODO: handle exception
						System.out.println(e.getMessage());
					}
        			
        				//return false;
        				
        			
				} catch (Exception e) {
					// TODO: handle exception
//					System.out.println(input.prevTxHash.toString());
					return false;
				}
    			i++;
    		}
    	}
//    	for (UTXO utxo : this.utxoPool.getAllUTXO()) {
//			if (utxo.getTxHash() == tx.getHash()) {
//				System.out.println("TX hash: " + tx.getHash());
//			}
//		}
		if (outputs != null) {
			
//			for (int j = 0; j < outputs.size(); j++) {
//				UTXO utxo = new UTXO(tx.getHash(), j);
//				if (!this.utxoPool.contains(utxo)) {
//					return false;
//				}
////				Transaction.Output output = tx.getOutput(i);
////				if (output.value < 0) return false;
////				outputSum += output.value ;
//			}
			int index = 0;
			
			for (Transaction.Output  output : outputs) {
//				if (output.value < 0) {
//					System.out.println(output.value );
//				}
				
				if (output.value < 0) return false;
				outputSum += output.value ;
				
//				UTXO utxo = new UTXO(tx.getHash(),index);
//				if (!this.utxoPool.contains(utxo)) return false;
				index++;
			}
		}
//		
    	if (inputSum < outputSum) return false;
    	
    	return ok;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	
    	ArrayList<Transaction> goodTransactions = new ArrayList<Transaction>();
    	//int i = 0;
    	ArrayList<UTXO> spentUtxos = new ArrayList<UTXO>();
    	ArrayList<UTXO> badUtxos = new ArrayList<UTXO>();
//    	System.out.println("UTXOpool before:");
//    	for (UTXO utxo : this.utxoPool.getAllUTXO()) {
//    		Transaction.Output output = this.utxoPool.getTxOutput(utxo);
//			System.out.println("------------Address: " + output.address + ", value: " + output.value);
//		}
    	int utxoPoolIndex = 0;
    	for (Transaction transaction : possibleTxs) {
    		if (transaction == null) continue;
    		
    		ArrayList<Transaction.Output> outputs = transaction.getOutputs();
    		System.out.println("utxoPoolIndex: " + utxoPoolIndex);
    		if(!isValidTx(transaction )) {
				
				for (int j = 0; j < outputs.size(); j++) {
					UTXO utxo = new UTXO(transaction.getHash(), j);
					
					if (this.utxoPool.contains(utxo)) {
//						this.utxoPool.removeUTXO(utxo);
						badUtxos.add(utxo);
					}
				}
			}
			else {
				boolean ok = true;
				ArrayList<Transaction.Input> inputs = transaction.getInputs();
				
				for (Transaction.Input input : inputs) {
					UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex );
				//	 System.out.println("OK utxo: " + utxo.getTxHash().toString() + ", " + utxo.getIndex());
				//	System.out.println("Not removed utxo: " + transaction.getHash() + ", " + utxoPoolIndex);
//					Transaction.Output output = utxoPool.getTxOutput(utxo);
					if (spentUtxos.contains(utxo)) {
						ok = false;
						for (Transaction.Output output : outputs) {
//							System.out.println("Transaction hash: " + transaction.getHash() + ", at: " + utxoPoolIndex);
							UTXO utxoRemove = new UTXO(transaction.getHash(),utxoPoolIndex);
							System.out.println("Removing utxo: " + transaction.getHash() + ", at: " + utxoPoolIndex);
							badUtxos.add(utxoRemove);
							utxoPoolIndex++;
						}
						
					}
					else {
						spentUtxos.add(utxo);
						for (Transaction.Output output : outputs) {
//							System.out.println("Transaction hash: " + transaction.getHash() + ", at: " + utxoPoolIndex);
//							UTXO utxoRemove = new UTXO(transaction.getHash(),utxoPoolIndex);
//							System.out.println("Removing utxo: " + transaction.getHash() + ", at: " + utxoPoolIndex);
//							badUtxos.add(utxoRemove);
							utxoPoolIndex++;
						}
					}
					

					
				}

				if (ok) {
					goodTransactions.add(transaction);
					
				}
				
				

			}
    		
		}
    	for (UTXO utxo : badUtxos) {
			this.utxoPool.removeUTXO(utxo);
		}
//    	System.out.println("UTXOpool after:");
//    	for (UTXO utxo : this.utxoPool.getAllUTXO()) {
//    		Transaction.Output output = this.utxoPool.getTxOutput(utxo);
//			System.out.println("------------Address: " + output.address + ", value: " + output.value);
//		}
    	Transaction[] transactions = new Transaction[goodTransactions.size()];
    	int i = 0;
    	for (Transaction transaction2 : goodTransactions) {
			transactions[i] = transaction2 ;
    		i++;
		}
    	
    	
    	return transactions ;
    }

}

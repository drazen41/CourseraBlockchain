/*
 * Main test code for Cousera cryptocurrency assignment1
 * Based on code by Sven Mentl and Pietro Brunetti
 * 
 * Copyright:
 * - Sven Mentl
 * - Pietro Brunetti
 * - Bruce Arden
 * - Tero Keski-Valkama
 */

import java.math.BigInteger;
import java.security.*;

public class Main {

   public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        /*
         * Generate key pairs, for Scrooge & Alice
         */
        KeyPair pk_scrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_alice   = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_bob = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        /*
         * Set up the root transaction:
         *
         * Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
         * By thin air I mean that this tx will not be validated, I just need it to get
         * a proper Transaction.Output which I then can put in the UTXOPool, which will be passed
         * to the TXHandler.
         */
        Tx tx = new Tx();
        tx.addOutput(10, pk_scrooge.getPublic());
        
        // This value has no meaning, but tx.getRawDataToSign(0) will access it in prevTxHash;
        byte[] initialHash = BigInteger.valueOf(0).toByteArray();
        tx.addInput(initialHash, 0);

        tx.signTx(pk_scrooge.getPrivate(), 0);

        /*
         * Set up the UTXOPool
         */
        // The transaction output of the root transaction is the initial unspent output.
        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx.getHash(),0);
        utxoPool.addUTXO(utxo, tx.getOutput(0));

        /*  
         * Set up a test Transaction
         */
        Tx tx2 = new Tx();

        // the Transaction.Output of tx at position 0 has a value of 10
        tx2.addInput(tx.getHash(), 0);
        
        // I split the coin of value 10 into 3 coins and send all of them for simplicity to
        // the same address (Alice)
        tx2.addOutput(5, pk_alice.getPublic());
        tx2.addOutput(3, pk_alice.getPublic());
        tx2.addOutput(2, pk_alice.getPublic());
       
        
        // Note that in the real world fixed-point types would be used for the values, not doubles.
        // Doubles exhibit floating-point rounding errors. This type should be for example BigInteger
        // and denote the smallest coin fractions (Satoshi in Bitcoin).

        // There is only one (at position 0) Transaction.Input in tx2
        // and it contains the coin from Scrooge, therefore I have to sign with the private key from Scrooge
        tx2.signTx(pk_scrooge.getPrivate(), 0);
        UTXOPool utxoPool2 = new UTXOPool();
        UTXO utxo2 = new UTXO(tx2.getHash(), 0);
        utxoPool2.addUTXO(utxo2, tx2.getOutput(0));
        UTXO utxo3 = new UTXO(tx2.getHash(), 1);
        utxoPool2.addUTXO(utxo3, tx2.getOutput(1));
        UTXO utxo4 = new UTXO(tx2.getHash(), 2);
        utxoPool2.addUTXO(utxo4, tx2.getOutput(2));
        /*
         * Start the test
         */
        // Remember that the utxoPool contains a single unspent Transaction.Output which is
        // the coin from Scrooge.
        TxHandler txHandler = new TxHandler(utxoPool);
        
        System.out.println("txHandler.isValidTx(tx2) returns: " + txHandler.isValidTx(tx2));
        System.out.println("txHandler.handleTxs(new Transaction[]{tx2}) returns: " +
            txHandler.handleTxs(new Transaction[]{tx2}).length + " transaction(s)");
       
        TxHandler txHandler2 = new TxHandler(utxoPool2);
        Transaction[] transactions = new Transaction[5];
        transactions[0] = tx;
        transactions[1] = tx2;
        Transaction[] handled = txHandler2.handleTxs(transactions );
        System.out.println(handled.length);
        
        // double spend
        Transaction[] transactions2 = new Transaction[5];
        Tx tx3 = new Tx();
        tx3.addInput(tx2.getHash(), 0);
        
        tx3.addOutput(3, pk_bob.getPublic());
        tx3.addOutput(2, pk_scrooge.getPublic());
        tx3.signTx(pk_alice.getPrivate(), 0);
        UTXOPool utxoPool3 = new UTXOPool();
        UTXO utxo5 = new UTXO(tx3.getHash(), 0);
        utxoPool3.addUTXO(utxo5 , tx3.getOutput(0));
        UTXO utxo6 = new UTXO(tx3.getHash(), 1);
        utxoPool3.addUTXO(utxo6 , tx3.getOutput(1));
        // double spend transactions
        Tx tx4 = new Tx();
        tx4.addInput(tx2.getHash(), 0);
        tx4.addOutput(2, pk_bob.getPublic());
        tx4.signTx(pk_alice.getPrivate(), 0);
        UTXO utxo7 = new UTXO(tx4.getHash(), 0);
        utxoPool3.addUTXO(utxo7, tx4.getOutput(0));
        
        Tx tx5 = new Tx();
        tx5.addInput(tx2.getHash(), 1);
        tx5.addOutput(2, pk_alice.getPublic());
        tx5.signTx(pk_alice.getPrivate(), 0);
        UTXO utxo8 = new UTXO(tx4.getHash(), 0);
        utxoPool3.addUTXO(utxo8, tx5.getOutput(0));
        
       
        
        transactions2[0] = tx3;
        transactions2[1] = tx4;
        transactions2[2] = tx5;
        TxHandler txHandler3 = new TxHandler(utxoPool3);
        Transaction[] handled2 = txHandler3.handleTxs(transactions2 );
        System.out.println(handled2.length);
    }


    public static class Tx extends Transaction { 
        public void signTx(PrivateKey sk, int input) throws SignatureException {
            Signature sig = null;
            try {
                sig = Signature.getInstance("SHA256withRSA");
                sig.initSign(sk);
                sig.update(this.getRawDataToSign(input));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            this.addSignature(sig.sign(),input);
            // Note that this method is incorrectly named, and should not in fact override the Java
            // object finalize garbage collection related method.
            this.finalize();
        }
    }
}

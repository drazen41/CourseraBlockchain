package Blockchain;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import Blockchain.Main.Tx;

public class Test {
	public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		 KeyPair pk_scrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
	     KeyPair pk_alice   = KeyPairGenerator.getInstance("RSA").generateKeyPair();
	     KeyPair pk_bob     = KeyPairGenerator.getInstance("RSA").generateKeyPair();
	     
	     
	    Block genesis = new Block(null, pk_scrooge.getPublic());
	 	genesis.finalize();
	 	BlockChain_No bc = new BlockChain_No(genesis);
	 	BlockHandler bh = new BlockHandler(bc);
	 	
	 	Block block1 = new Block(genesis.getHash(), pk_alice.getPublic());
	 	Tx tx1 = new Tx();
        tx1.addInput(genesis.getCoinbase().getHash(), 0);
        tx1.addOutput(5, pk_alice.getPublic());
        tx1.addOutput(10, pk_alice.getPublic());
        tx1.addOutput(10, pk_alice.getPublic());
        tx1.signTx(pk_scrooge.getPrivate(), 0);
		block1.addTransaction(tx1);
		block1.finalize();
	 	bc.addBlock(block1);
	 	
	 	
	 	Block block2 = bh.createBlock(pk_alice.getPublic());
	 	
		
		
		
//		Block block11 = new Block(block1.getHash(), pk_bob.getPublic());
//	 	Tx tx2 = new Tx();
//       tx2.addInput(block1.getCoinbase().getHash(), 0); 
//       tx2.addOutput(2, pk_scrooge.getPublic());
//       tx2.addOutput(3, pk_scrooge.getPublic());
//       tx2.addOutput(4, pk_scrooge.getPublic());
//       tx2.signTx(pk_alice.getPrivate(), 0);
//		block11.addTransaction(tx2);
//		block11.finalize();
//		TreeNode treeNode11  = new TreeNode(block11);
//		treeNode1.setChild(treeNode11);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		System.out.println("Block1 Added ok: " + bh.processBlock(block1)); // Ovdje kreirati UTXO pool
	}
}

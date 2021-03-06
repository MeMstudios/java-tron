/*
 * java-tron is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-tron is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.tron.peer;

import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.command.ConsensusCommand;
import org.tron.config.Configer;
import org.tron.consensus.client.Client;
import org.tron.core.Blockchain;
import org.tron.core.PendingStateImpl;
import org.tron.core.TransactionUtils;
import org.tron.core.UTXOSet;
import org.tron.crypto.ECKey;
import org.tron.example.Tron;
import org.tron.protos.core.TronBlock;
import org.tron.protos.core.TronTransaction;
import org.tron.utils.ByteArray;
import org.tron.wallet.Wallet;
import org.tron.overlay.Net;

public class Peer {
    public final static String PEER_NORMAL = "normal";
    public final static String PEER_SERVER = "server";

    private Blockchain blockchain = null;
    private UTXOSet utxoSet = null;
    private Wallet wallet = null;
    private static Peer INSTANCE = null;
    private String type;
    private final ECKey myKey = Configer.getMyKey();

    public Peer(String type) {
        this();

        this.type = type;

        init();
    }

    public Peer(){
    }

    public void addReceiveTransaction(String message) {
        try {
            TronTransaction.Transaction transaction = TronTransaction
                    .Transaction.parseFrom(ByteArray
                            .fromHexString(message));
            System.out.println(TransactionUtils.toPrintString
                    (transaction));
            PendingStateImpl pendingState =new PendingStateImpl();
//            PendingStateImpl pendingState = (PendingStateImpl)
//                    blockchain.getPendingState();
            pendingState.addPendingTransaction(blockchain, transaction);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void addReceiveBlock(String message) {
        TronBlock.Block block = null;
        try {
            block = TronBlock.Block.parseFrom(ByteArray.fromHexString(message
                    ));
            blockchain.receiveBlock(block, utxoSet, this);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public static Peer getInstance(String type) {
        if (INSTANCE == null) {
            INSTANCE = new Peer(type);
        }
        return INSTANCE;
    }

    private void init() {
        initWallet();
        initBlockchian();
        initUTXOSet();
        initLoadBlock();
    }

    private void initLoadBlock(){
        if (this.type.equals(Peer.PEER_NORMAL)){
            System.out.println("BlockChain loading  ...");
            Client.loadBlock(this);
        }
    }

    private void initBlockchian() {
        new ConsensusCommand().server();
        if (Blockchain.dbExists()) {
            blockchain = new Blockchain();
        } else {
            //blockchain = new Blockchain(ByteArray.toHexString(wallet.getAddress()));
            blockchain = new Blockchain(ByteArray.toHexString(wallet
                    .getAddress()), this.type);
        }
    }

    private void initUTXOSet() {
        utxoSet = new UTXOSet();
        utxoSet.setBlockchain(blockchain);
        utxoSet.reindex();
    }

    private void initWallet() {
        wallet = new Wallet();
        wallet.init(myKey);
    }

    public ECKey getMyKey() {
        return myKey;
    }

    public Wallet getWallet() {
        return wallet;
    }
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }
    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public UTXOSet getUTXOSet() {
        return utxoSet;
    }
    public void setUTXOSet(UTXOSet utxoSet) {
        this.utxoSet = utxoSet;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Net getNet() {
        return null;
        //return net;
    }
}

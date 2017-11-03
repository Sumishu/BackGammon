
package backgammon;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/*

SDP Project
Project Name: BackGammon

Members: 
1. Al Nasirullah Siddiky
2. Safayat Ullah
3. Rajeeb Talukder

*/


class Dice                                //Author: Rajeeb Talukder
{
    int dice1,dice2;
    public Dice(int dice1, int dice2)
    {
        this.dice1=dice1;
        this.dice2=dice2;
    }
}


class RandomDiceRoller                    //Author: Al Nasirullah Siddiky
{
    private static RandomDiceRoller randomDiceRoller;
    Dice dice;
    Random random;
    private RandomDiceRoller()
    {
        random = new Random();
    }
    public static RandomDiceRoller getInstance()
    {
        if(randomDiceRoller==null)
            randomDiceRoller=new RandomDiceRoller();
        return randomDiceRoller;
    }

    public Dice getDice()
    {
        dice = new Dice(random.nextInt(6)+1, random.nextInt(6)+1);
        return dice;
    }
}


class ImageIconCreator                    //Author: Safayat Ullah
{
    private static ImageIconCreator imageIconCreator=null;
    private ImageIconCreator()
    {

    }
    public static ImageIconCreator getInstance()
    {
        if(imageIconCreator==null)
            imageIconCreator=new ImageIconCreator();
        return imageIconCreator;
    }
    public ImageIcon getImageIcon(boolean type, int cnt, int position)
    {
        String st;
        //down row
        if(cnt==0)
            st="00.jpg";
        else if(position<13 || position==27){
            if(type==false)
                st="D0"+cnt+".jpg";
            else
                st="D"+cnt+"0"+".jpg";
        }
        else{
            if(type==false)
                st="U0"+cnt+".jpg";
            else
                st="U"+cnt+"0"+".jpg";
        }
        return new ImageIcon(new String(st));
    }
}

class Move                                  //Author: Rajeeb Talukder
{
    int previousPosition,newPosition; //piecepile index
    public Move(int previousPosition, int newPosition){
        this.previousPosition=previousPosition;
        this.newPosition=newPosition;
    }
}


class PiecePile                             //Author: Safayat Ullah
{
    JButton btn;
    JLabel lbl;
    ImageIcon icon;
    int position;
    int pieceCount;
    boolean type;
    boolean isClickable;
    GameBoard gameBoard;
    public PiecePile(int position, int pieceCount, boolean type, GameBoard gameBoard){ //type 0 green 1 red, count 0, null
        icon=ImageIconCreator.getInstance().getImageIcon(type,pieceCount,position);
        lbl=new JLabel(icon);
        btn=new JButton(icon);


        this.position=position;
        this.pieceCount=pieceCount;
        this.type=type;

        isClickable=false;
        this.gameBoard=gameBoard;
        gameBoard.add(lbl);
        gameBoard.repaint();
    }

    public final void setEnableAsButton(boolean flag)
    {
        if(flag==isClickable){
            //already in mood
        }
        else{
            isClickable=flag;
            if(flag){
                gameBoard.remove(lbl);
                gameBoard.add(btn);
            }
            else{
                for(MouseListener ml : btn.getMouseListeners()){
                    btn.removeMouseListener(ml);
                }
                gameBoard.remove(btn);
                gameBoard.add(lbl);
            }

            gameBoard.repaint();
        }
    }
    public void addPiece(boolean type){
        pieceCount++;
        this.type=type;
        updateIcon();
    }
    public void removeTopPiece()
    {
        pieceCount--;
        if(pieceCount==0)
            type=false;
        updateIcon();
    }
    private void updateIcon()
    {
        icon=ImageIconCreator.getInstance().getImageIcon(type, pieceCount,position);
        lbl.setIcon(icon);
        btn.setIcon(icon);

        gameBoard.repaint();
    }
    public void setBounds(int x, int y, int h,int w)
    {
        lbl.setBounds(x,y,h,w);
        btn.setBounds(x,y,h,w);
    }

}
 
class OpponentGenerator                         //Author: Al Nasirullah Siddiky
{
    private Opponent opponent;
    public OpponentGenerator()
    {

    }
    public Opponent generateOpponent(boolean isSinglePlayer, boolean firstTurn)
    {
        if(isSinglePlayer){
            opponent= new AutoPlayer(firstTurn);
        }
        else{
            System.out.println("What do you want to be?\n1. Server\n2. Client\n");
            Scanner sc=new Scanner(System.in);
            int x=sc.nextInt();

            opponent = new NetworkPlayer(x==1,firstTurn);
        }
        return opponent;
    }

}

abstract class Opponent                             //Author: Rajeeb Talukder
{
    boolean firstTurn;
    public Opponent(boolean firstTurn)
    {
        this.firstTurn=firstTurn;
    }
    public abstract Dice sendDice();
    public abstract void receiveDice(Dice dice);
    public abstract Move[] sendMove();
    public abstract void receiveMove(Move[] mov);
}

class AutoPiecePile //Author: Rajeeb Talukder
{
    int pieceCount;
    boolean type;
    public AutoPiecePile(int count, boolean type)
    {
        pieceCount=count;
        this.type=type;
    }
    public void addPiece(boolean type)
    {
        pieceCount++;
        this.type=type;
    }
    public void removeTopPile()
    {
        if(pieceCount==0){ //this should not happen actully
            System.out.println("\nCan not remove file from empty file!");
        }
        pieceCount--;
        if(pieceCount==0)
            type=false;
    }
}


class AutoPlayer extends Opponent                 //Author: Safayat Ullah
{
    RandomDiceRoller randomDice;
    AutoPiecePile[] piecePile;
    Dice dice,dicep;
    int indx[];
    public AutoPlayer(boolean firstTurn)
    {
        super(firstTurn);
        randomDice = RandomDiceRoller.getInstance();
        indx =new int[28];
        indx[0]=27;
        for(int i=1;i<=24;i++){
            indx[i]=25-i;
        }
        indx[25]=26;

        indx[26]=25;
        indx[27]=0;
        //done

        piecePile = new AutoPiecePile[28];

        initializePiecePile();
        printPiece();
    }
    public boolean ok()
    {
        int a=0,b=0;
        for(int i=0;i<28;i++){
            if(piecePile[i].type==false)
                a+=piecePile[i].pieceCount;
            else
                b+=piecePile[i].pieceCount;
        }
        if(a==b && a==15)
            return true;
        else{
            System.out.println("\n*****. "+a+" "+b);
            return false;
        }
    }
    private void initializePiecePile()
    {
        for(int i=0;i<28;i++){
            piecePile[i] =null;
        }

        piecePile[1] = new AutoPiecePile(2,true);
        piecePile[24] = new AutoPiecePile(2,false);

        piecePile[6] = new AutoPiecePile(5,false);
        piecePile[19] = new AutoPiecePile(5,true);

        piecePile[8] = new AutoPiecePile(3,false);
        piecePile[17] = new AutoPiecePile(3,true);

        piecePile[12] = new AutoPiecePile(5,true);
        piecePile[13] = new AutoPiecePile(5,false);

        for(int i=0;i<28;i++){
            if(piecePile[i]==null){
                piecePile[i]=new AutoPiecePile(0,false);
            }
        }
    }
    private void printPiece()
    {
        for(int i=0;i<28;i++){
            if(piecePile[i].pieceCount>0)
            System.out.println("\n"+indx[i]+": "+!(piecePile[i].type)+" "+piecePile[i].pieceCount);
        }
    }
    public Dice RollDice()
    {
        dice=randomDice.getDice();
        return dice;
    }
    private boolean isPocketPossible()
    {
        int i,cnt=0;
        for(i=0;i<=6;i++){
            if(piecePile[i].type==false)
                cnt+=piecePile[i].pieceCount;
        }
        return cnt==15;
    }
    public int max(int a, int b)
    {
        if(a>b)
            return a;
        else 
            return b;
    }
    
    public Move[] getOptimalMove() // you have the value of dice now!
    {

        Move[] mov = new Move[9];
        boolean movZero=isPocketPossible();
        
        for(int i=0;i<9;i++)
            mov[i]=null;
        if(!ok()){
            return mov;
        }
        //printPiece();
        if(dice.dice1==0 && dice.dice2==0){
            dice=dicep;
        }
        else
            dicep=new Dice(dice.dice1,dice.dice2);
        if(dice.dice1>dice.dice2){
            int x=dice.dice1;
            dice.dice1=dice.dice2;
            dice.dice2=x;
        }
        
        int j=0;
        for(int i=25; i>0 && (dice.dice1>0 || dice.dice2>0); i--){
            if(piecePile[i].type==false && piecePile[i].pieceCount>0){
                if(dice.dice1!=0 && (i-dice.dice1>0 || movZero) && (piecePile[max(0,i-dice.dice1)].type==false || piecePile[max(0,i-dice.dice1)].pieceCount<2)){
                    mov[j++]=new Move(convertIndexToOther(i),convertIndexToOther(max(0,i-dice.dice1)));
                    //System.out.println("\nmoveed "+i+" to "+max(0,i-dice.dice1));
                    //System.out.println("\nConvmoveed "+convertIndexToOther(i)+" to "+convertIndexToOther(max(0,i-dice.dice1)));

                    if(piecePile[max(0,i-dice.dice1)].type==true && piecePile[max(0,i-dice.dice1)].pieceCount==1){
                        piecePile[26].addPiece(true);
                        piecePile[max(0,i-dice.dice1)].removeTopPile();
                    }
                    piecePile[max(0,i-dice.dice1)].addPiece(false);
                    piecePile[i].removeTopPile();
                    dice.dice1=0;
                }
                if(piecePile[i].type==false && piecePile[i].pieceCount>0 && dice.dice2!=0 && (i-dice.dice2>0 || movZero) && (piecePile[max(0,i-dice.dice2)].type==false || piecePile[max(0,i-dice.dice2)].pieceCount<2)){
                    mov[j++]=new Move(convertIndexToOther(i),convertIndexToOther(max(0,i-dice.dice2)));
                    //System.out.println("\nmoved "+i+" to "+(i-dice.dice2));
                    //System.out.println("\nConverted move "+convertIndexToOther(i)+" to "+convertIndexToOther(i-dice.dice2));

                    if(piecePile[max(0,i-dice.dice2)].type==true && piecePile[max(0,i-dice.dice2)].pieceCount==1){
                        piecePile[26].addPiece(true);
                        piecePile[max(0,i-dice.dice2)].removeTopPile();
                    }
                    piecePile[max(0,i-dice.dice2)].addPiece(false);
                    piecePile[i].removeTopPile();
                    dice.dice2=dice.dice1;
                    dice.dice1=0;
                }
            }
        }
        if(j==0){
            dice.dice1=0;
            dice.dice2=0;
            dicep.dice1=0;
            dicep.dice2=0;
        }
        //printPiece();
        return mov;
    }
    private int convertIndexToMe(int i)
    {
        return indx[i];
    }
    private int convertIndexToOther(int i)
    {
        return indx[i];
    }
    public void receiveMove(Move[] mov)
    {
        if(!ok()){
            //that means there's a serious bug in the code!
            System.out.println("\n****Move rejected****");
            return;
        }
        //System.out.println("\nBefore opponent");
        //printPiece();
        int i;
        Move cMove;
        for(i=0;mov[i]!=null;i++){
            cMove=mov[i];
            System.out.println("\nGot Move: "+cMove.previousPosition+" "+cMove.newPosition);
            cMove=new Move(convertIndexToMe(cMove.previousPosition),convertIndexToMe(cMove.newPosition));
            System.out.println("\nconv Move : "+cMove.previousPosition+" "+cMove.newPosition);
            if(piecePile[cMove.newPosition].type==false && piecePile[cMove.newPosition].pieceCount==1){
                piecePile[25].addPiece(false);
                piecePile[cMove.newPosition].removeTopPile();
            }
            piecePile[cMove.newPosition].addPiece(true);
            piecePile[cMove.previousPosition].removeTopPile();
       }
        //System.out.println("\nafter opponent");
        //printPiece();
    }
    public Move[] sendMove()
    {
        return getOptimalMove();
    }
    public void receiveDice(Dice dice)
    {
        //do nothing, only move matters! :p
    }
    public Dice sendDice()
    {
        dice=randomDice.getDice();
        return dice;
    }
}


class NetworkPlayer extends Opponent                      //Author: Safayat Ullah
{
    ServerSocket server;
    Socket socket;
    BufferedReader in;
    BufferedWriter out;
    PiecePile[] piecePile;
    Dice dice;
    BoardHandler boardHandler;
    int ind[];
    NetworkPlayer(boolean isServer, boolean firstTurn) {
        super(firstTurn);
        ind=new int[28];
        ind[0]=27;
        ind[27]=0;
        ind[25]=26;
        ind[26]=25;
        for(int i=1;i<=24;i++)
            ind[i]=25-i;
        if(isServer){
            try {
                server  = new ServerSocket(8182);
                socket= server.accept();
                in= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException ex) {
                Logger.getLogger(NetworkPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            try {
                server=null;
                socket=new Socket("localhost",8182);
                in= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException ex) {
                Logger.getLogger(NetworkPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //Author: Al Nasirullah Siddiky
    @Override
    public Dice sendDice(){
        Dice dice=null;
        try {
            dice=new Dice(in.read(),in.read());

        } catch (IOException ex) {
            Logger.getLogger(NetworkPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dice;
    }

    //Author: Al Nasirullah Siddiky
    @Override
    public void receiveDice(Dice dice) {
        try {
            out.write(dice.dice1);
            out.flush();
            out.write(dice.dice2);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(NetworkPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Author: Safayat Ullah
    @Override
    public Move[] sendMove() {
        Move[]  mov = new Move[9];
        try {
            boolean fl=true;
            for(int i=0;fl;i++){
                mov[i]=new Move(in.read(),in.read());
                if(mov[i].previousPosition==-1){
                    mov[i]=null;
                    fl=false;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(NetworkPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mov;
    }

    //Author: Safayat Ullah
    @Override
    public void receiveMove(Move[] mov) {
        try {
            for(int i=0;mov[i]!=null;i++){
                out.write(mov[i].previousPosition);
                out.flush();
                out.write(mov[i].newPosition);
                out.flush();
            }
            out.write(-1);
            out.flush();
            out.write(-1);
            out.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(NetworkPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

class BoardHandler                                        //Author: Safayat Ullah
{
    PiecePile[] piecePile;
    GameBoard gameBoard;
    Move[] move;
    int moveCounter;

    public BoardHandler(PiecePile[] piecePile, GameBoard gameBoard){
        this.gameBoard=gameBoard;
        this.piecePile=piecePile;
        move=new Move[100];
        moveCounter=0;
    }
    private void orderPiecePile()
    {
        for(int i=0;i<6;i++){
            piecePile[12-i].setBounds(30+i*65,410,60,160);
            piecePile[6-i].setBounds(30+65*7+i*65,410,60,160);

            piecePile[13+i].setBounds(30+i*65,30,60,160);
            piecePile[19+i].setBounds(30+65*7+i*65,30,60,160);
        }

        piecePile[0].setBounds(30+13*65+5,360,60,160);
        piecePile[27].setBounds(30+13*65+5,80,60,160);

        piecePile[25].setBounds(30+6*65,350,60,160);
        piecePile[26].setBounds(30+6*65,90,60,160);
    }
    public void recordNewMove(Move mov)
    {
        move[moveCounter]=mov;
        moveCounter++;
    }
    public void clearMove()
    {
        moveCounter=0;
        for(int i=0;i<9;i++)
            move[i]=null;
    }
    public void getInitialPiecePile()
    {
        for(int i=0;i<28;i++){
            piecePile[i] =null;
        }

        piecePile[1] = new PiecePile(1,2,true,gameBoard);
        piecePile[24] = new PiecePile(24,2,false,gameBoard);

        piecePile[6] = new PiecePile(6,5,false,gameBoard);
        piecePile[19] = new PiecePile(19,5,true,gameBoard);

        piecePile[8] = new PiecePile(8,3,false, gameBoard);
        piecePile[17] = new PiecePile(17,3,true,gameBoard);

        piecePile[12] = new PiecePile(12,5,true,gameBoard);
        piecePile[13] = new PiecePile(13,5,false, gameBoard);

        for(int i=0;i<28;i++){
            if(piecePile[i]==null){
                piecePile[i]=new PiecePile(i,0,false, gameBoard);
            }
        }

        orderPiecePile();
    }
    
    
    public void changeFromMove(Move[] mov) //from opposite
    {
        for(int i=0;mov[i]!=null;i++){
            System.out.println("\nReceived.. Move "+mov[i].previousPosition+" to "+mov[i].newPosition);
            if(piecePile[mov[i].newPosition].type==false && piecePile[mov[i].newPosition].pieceCount==1){
                System.out.println("\nCUTT."+mov[i].newPosition);
                piecePile[mov[i].newPosition].removeTopPiece();
                piecePile[25].addPiece(false);
                gameBoard.repaint();
                try{
                    sleep(2000);
                }
                catch(Exception e){
                    System.out.println("Error at sleep: "+e.toString());
                }
            }
            piecePile[mov[i].newPosition].addPiece(true);
            piecePile[mov[i].previousPosition].removeTopPiece();
            gameBoard.repaint();
            try{
                sleep(2000);
            }
            catch(Exception e){
                System.out.println("Error at sleep: "+e.toString());
            }
        }
    }

    public boolean showOptionsFromDice(Dice dice2)
    {
        if(dice2.dice1==0 && dice2.dice2==0){
            gameBoard.okclicked=true;
            return false;
        }
        gameBoard.okclicked=false;
        Dice dice=dice2;
        int i;
        boolean anyMovePossible=false;
        for(i=7;i<26;i++)
            if(piecePile[i].type==false && piecePile[i].pieceCount>0)
                break;
        if(i==26){
            for(i=1;i<7;i++){
                if(piecePile[i].type==false && piecePile[i].pieceCount>0 && (i<=dice.dice1 || i<=dice.dice2)){
                    gameBoard.cnter=i;
                    //System.out.println("\n"+i+" is clickabe");
                    piecePile[i].setEnableAsButton(true);
                    piecePile[i].btn.addMouseListener(new MouseListener(){
                        final int j=gameBoard.cnter;
                        @Override
                        public void mouseClicked(MouseEvent e){
                            //System.out.println("\nPRESSED"+j);

                            setAllNotClickable();

                            showOptionAfterAclick(dice,j);
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                        }

                    });
                    anyMovePossible=true;
                }
            }
        }
        for(i=1;i<26;i++){
            if(piecePile[i].pieceCount>0 && piecePile[i].type==false){
                if(dice.dice1>0 && i-dice.dice1>0 && (piecePile[i-dice.dice1].type!=true || piecePile[i-dice.dice1].pieceCount<2)){
                    gameBoard.cnter=i;
                    //System.out.println("\n"+i+" is clickabe");
                    piecePile[i].setEnableAsButton(true);
                    piecePile[i].btn.addMouseListener(new MouseListener(){
                        final int j=gameBoard.cnter;
                        @Override
                        public void mouseClicked(MouseEvent e){
                            //System.out.println("\nPRESSED"+j);
                            setAllNotClickable();

                            showOptionAfterAclick(dice,j);
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                        }

                    });
                    anyMovePossible=true;

                }
                else if(dice.dice2>0 && i-dice.dice2>0 && (piecePile[i-dice.dice2].type!=true || piecePile[i-dice.dice2].pieceCount<2)){
                    gameBoard.cnter=i;
                    //System.out.println("\n"+i+" is clickabe");
                    piecePile[i].setEnableAsButton(true);
                    piecePile[i].btn.addMouseListener(new MouseListener(){
                        final int j=gameBoard.cnter;
                        @Override
                        public void mouseClicked(MouseEvent e){
                            //System.out.println("\nPRESSED.."+j);
                            setAllNotClickable();
                            showOptionAfterAclick(dice,j);
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                        }

                    });
                    anyMovePossible=true;
                }
            }
        }
        return anyMovePossible;
    }
    public void setAllNotClickable()
    {
        int i;
        for(i=0;i<28;i++){
            piecePile[i].setEnableAsButton(false);
        }
        gameBoard.repaint();
    }
    public void showOptionAfterAclick(Dice dice, int x)
    {
        int i;
        for(i=7;i<26;i++)
            if(piecePile[i].type==false && piecePile[i].pieceCount>0)
                break;
        if(i==26){              //if move to pocket is possible
            if(piecePile[x].type==false && (x<=dice.dice1 || x<=dice.dice2)){
                gameBoard.cnter=0;
                //System.out.println("\n"+0+" is clickabe");
                piecePile[0].setEnableAsButton(true);
                piecePile[0].btn.addMouseListener(new MouseListener(){
                    final int j=gameBoard.cnter;
                    @Override
                    public void mouseClicked(MouseEvent e){
                        System.out.println("\nMoved "+x+" to "+j);
                        piecePile[x].removeTopPiece();
                        piecePile[j].addPiece(piecePile[x].type);
                        recordNewMove(new Move(x,j));
                        setAllNotClickable();
                        if(x<=dice.dice1)
                            dice.dice1=0;
                        else{
                            dice.dice2=dice.dice1;
                            dice.dice1=0;
                        }
                        if(showOptionsFromDice(dice));
                   }
                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }

                });
            }
        }

        if(dice.dice1!=0){
            if(x-dice.dice1>0 && (piecePile[x-dice.dice1].type==false || piecePile[x-dice.dice1].pieceCount<2)){
                gameBoard.cnter=x-dice.dice1;
                //System.out.println("\n"+(x-dice.dice1)+" is clickabe");
                piecePile[x-dice.dice1].setEnableAsButton(true);
                piecePile[x-dice.dice1].btn.addMouseListener(new MouseListener(){
                    final int j=gameBoard.cnter;
                    final boolean fl=(piecePile[j].type && piecePile[j].pieceCount==1);
                    @Override
                    public void mouseClicked(MouseEvent e){
                        System.out.println("\nMoved "+x+" to "+j);
                        recordNewMove(new Move(x,j));
                        piecePile[j+dice.dice1].removeTopPiece();
                        if(fl){
                            piecePile[j].removeTopPiece();
                            piecePile[26].addPiece(fl);
                        }
                        piecePile[j].addPiece(piecePile[j+dice.dice1].type);
                        setAllNotClickable();
                        dice.dice1=0;
                        if(showOptionsFromDice(dice));
                   }
                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
            }

        }
        if(dice.dice2!=0 && dice.dice2!=dice.dice1){
            if(x-dice.dice2>0 && (piecePile[x-dice.dice2].type==false || piecePile[x-dice.dice2].pieceCount<2)){
                gameBoard.cnter=x-dice.dice2;
                //System.out.println("\n"+(x-dice.dice2)+" is clickabe");
                piecePile[x-dice.dice2].setEnableAsButton(true);
                piecePile[x-dice.dice2].btn.addMouseListener(new MouseListener(){
                    final int j=gameBoard.cnter;
                    final boolean fl=piecePile[j].type;
                    @Override
                    public void mouseClicked(MouseEvent e){
                        System.out.println("\nMoved "+x+" to "+j);
                        //piecePile[j].btn.removeMouseListener(this);
                        recordNewMove(new Move(x,j));
                        piecePile[j+dice.dice2].removeTopPiece();
                        if(fl){
                            piecePile[j].removeTopPiece();
                            piecePile[26].addPiece(fl);
                        }
                        piecePile[j].addPiece(piecePile[j+dice.dice2].type);
                        setAllNotClickable();
                        dice.dice2=dice.dice1;
                        dice.dice2=0;
                        if(showOptionsFromDice(dice));
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
            }
        }
    }
}


class GameBoard extends JFrame                            //Author: Safayat Ullah
{
    int cnter;
    PiecePile[] piecePileList;
    JLabel[] pieceIndex;
    JLabel[] dices;
    /*
        0 to 12 and 25 for lower layer. 0 is the pocket position for player 1, 25 dead for player 2
        and 13 to 24, 26,27  for upper layer 26 dead 27 for pocket position
        all are reflexive for the opponent
    */
    int[] pieceOnPocket;
    int player1Score, player2Score;
    BoardHandler boardHandler;
    boolean okclicked;
    public GameBoard()
    {
        super("BackGammon");
        setBounds(0,0,75+65*14,640);
        //getContentPane().setBackground(Color.GRAY);
        setLayout(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        piecePileList = new PiecePile[28];
        boardHandler = new BoardHandler(piecePileList,this);
        boardHandler.getInitialPiecePile();
        pieceIndex=new JLabel[28];
        for(int i=0;i<28;i++){
            pieceIndex[i] = new JLabel(""+i,SwingConstants.CENTER);
        }

        for(int i=0;i<6;i++){
            pieceIndex[12-i].setBounds(30+i*65,570,60,30);
            pieceIndex[6-i].setBounds(30+65*7+i*65,570,60,30);

            pieceIndex[13+i].setBounds(30+i*65,0,60,30);
            pieceIndex[19+i].setBounds(30+65*7+i*65,0,60,30);
        }

        pieceIndex[0].setBounds(30+13*65+5,520,60,30);
        pieceIndex[27].setBounds(30+13*65+5,50,60,30);

        pieceIndex[25].setBounds(30+6*65,510,60,30);
        pieceIndex[26].setBounds(30+6*65,60,60,30);

        pieceIndex[0].setText("Pocket1");
        pieceIndex[27].setText("Pocket2");
        pieceIndex[25].setText("Dead1");
        pieceIndex[26].setText("Dead2");


        for(int i=0;i<28;i++){
            add(piecePileList[i].lbl);
            add(pieceIndex[i]);
        }

        dices = new JLabel[4];

        for(int i=0;i<4;i++){ //0 and 1 for opponent, 2 and 3 for me
            dices[i]=new JLabel(getDiceImage(i+1));
        }

        dices[0].setBounds(30+120,275,50,50);
        dices[1].setBounds(30+120+50+45,275,50,50);
        dices[2].setBounds(30+65*7+120,275,50,50);
        dices[3].setBounds(30+65*7+120+50+45,275,50,50);

        repaint();
        okclicked=false;
    }

    public boolean isstarted()
    {
        return okclicked;
    }

    public boolean isEnd()
    {
        return (piecePileList[0].pieceCount==15 || piecePileList[27].pieceCount==15);
    }

    public void startPlay()
    {
        JButton start=new JButton("Play");
        start.setBounds(30+6*65-5,280,70,40);
        add(start);
        repaint();
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                okclicked=true;
                start.removeActionListener(this);
                remove(start);
                repaint();
                System.out.println("\nCLicked "+okclicked);
            }
        });

    }
    public void gameEndMessage(boolean win)
    {
        JLabel lab=new JLabel();
        if(win)
            lab.setText("YOU WIN!");
        else 
            lab.setText("YOU Lose");
        lab.setBounds(300,300,50,50);
        add(lab);
        repaint();
        try{
            sleep(5000);
        }
        catch(Exception e){
            System.out.println("Execption in sleep"+e.toString());
        }
    }
    public void showDices(Dice dice)
    {
        add(dices[0]);
        add(dices[1]);
    }

    private ImageIcon getDiceImage(int dice)
    {
        String name="d"+dice+".png";
        return new ImageIcon(name);
    }

    void changeFromeMove(Move[] move)
    {
        boardHandler.changeFromMove(move);
    }

    public void printDice(boolean isFirstPlayer, Dice dice)
    {
        if(isFirstPlayer){
            dices[2].setIcon(getDiceImage(dice.dice1));
            dices[3].setIcon(getDiceImage(dice.dice2));
            add(dices[2]);
            add(dices[3]);
        }
        else{
            dices[0].setIcon(getDiceImage(dice.dice1));
            dices[1].setIcon(getDiceImage(dice.dice2));
            add(dices[0]);
            add(dices[1]);
        }
        repaint();
    }
    public void removedice(boolean firstPlayer)
    {
        if(firstPlayer){
            remove(dices[2]);
            remove(dices[3]);
        }
        else{
            remove(dices[0]);
            remove(dices[1]);
        }
        repaint();
    }
}

class GameBoardPlayer implements Runnable                   //Author: Safayat Ullah
{
    BoardHandler boardHandler; //this should be in other place :/
    GameBoard gameBoard;
    Opponent opponent;
    boolean gameNotEnd;
    Dice dice;
    RandomDiceRoller diceRoller;
    public GameBoardPlayer(GameBoard gameBoard, Opponent opponent)
    {
        this.gameBoard=gameBoard;
        this.boardHandler=gameBoard.boardHandler;
        this.opponent=opponent;
        diceRoller = RandomDiceRoller.getInstance();
        gameNotEnd=true;
    }
    @Override
    public void run(){
        Move[] move;
        if(opponent.firstTurn){
            gameBoard.startPlay();
            while(!gameBoard.isstarted()){
                System.out.print(".");
            }
            gameBoard.repaint();
            
            while(gameNotEnd){
                if(myMove())
                    gameNotEnd=false;
                else if(opponentMove()){
                    gameNotEnd=false;
                }
            }
        }
        else{
            while(gameNotEnd){
                if(opponentMove()){
                    gameNotEnd=false;
                }
                else if(myMove())
                    gameNotEnd=false;
            }
        }
    }
    private boolean opponentMove()
    {
        Move[] move;
        Dice dice=opponent.sendDice();
        Dice dice2=new Dice(dice.dice1,dice.dice2);
        System.out.println("\nopponents dices are : "+dice.dice1+" "+dice.dice2);
        gameBoard.printDice(false, dice);
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(GameBoardPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        move =opponent.sendMove();
        gameBoard.changeFromeMove(move);
        gameBoard.repaint();
        if(dice2.dice1==dice2.dice2){
            move=opponent.sendMove();
            gameBoard.changeFromeMove(move);
            gameBoard.repaint();
        }
        gameBoard.removedice(false);
        if(gameBoard.isEnd()){
            gameBoard.gameEndMessage(true);
            return true; //game ends and you loose
        }
        return false; //game continues;
    }
    private boolean myMove()
    {
        Move[] move;
        Dice dice=diceRoller.getDice();
        System.out.println("\ndices are "+dice.dice1+" "+dice.dice2);
        gameBoard.printDice(true,dice);


        opponent.receiveDice(dice);
        Dice dice2=new Dice(dice.dice1,dice.dice2);
        move=makeMove(dice); //handle null pointet

        //gameBoard.changeFromeMove(move);

        opponent.receiveMove(move);

        if(dice2.dice1==dice2.dice2){
            move=makeMove(dice2);
            opponent.receiveMove(move);
        }
        gameBoard.removedice(true);

        if(gameBoard.isEnd()){
            gameBoard.gameEndMessage(true);
            return true; //game end and you won
        }
        return false; //game not end
    }
    private Move[] makeMove(Dice dice)
    {
        if(dice==null){
            return null;
        }
        boardHandler.clearMove();

        System.out.println("\noka");
        gameBoard.okclicked=false;
        if(boardHandler.showOptionsFromDice(dice)){
            while(gameBoard.okclicked==false){
                System.out.print(".");
            }
        }
        return boardHandler.move;
    }
}

public class BackGammon {
    public static void main(String[] args) {

        Opponent opponent=new OpponentGenerator().generateOpponent(true,true);
        GameBoard gameBoard = new GameBoard();
        ImageIconCreator imageIconCreator = ImageIconCreator.getInstance();
        GameBoardPlayer player=new GameBoardPlayer(gameBoard,opponent);
        player.run();
    }
}

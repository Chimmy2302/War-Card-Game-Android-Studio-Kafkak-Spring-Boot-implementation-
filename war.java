import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class war {
    private static List<String> arr = new ArrayList<>();
    private static List <String> order = new ArrayList<>(Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"));

    //creating the deck
    private static void FillDeck(){
        String [] types = {"H", "S", "D", "C"};
        String [] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        for(String type: types){
            for(String value: values){
                arr.add(value+type);
            }
        }
    }

    //handing out the cards
    private static void Dealer(List<String>deck1, List<String>deck2){
        Random rand = new Random(); 
        // Pick random card and remove from stack
        boolean dealing = true;
        while(dealing){
            if(!arr.isEmpty()){
                int index1 = rand.nextInt(arr.size());
                deck1.add(arr.get(index1));
                arr.remove(index1);
                int index2 = rand.nextInt(arr.size());
                deck2.add(arr.get(index2));
                arr.remove(index2);
            }
            else{
                dealing = false;
            }
        }
        return;
    }

    //the actual game
    private static int Play(List<String>deck1, List<String>deck2){
        boolean fight = true;
        int round = 0;
        List<String> holder1 = new ArrayList<>();
        List<String> holder2 = new ArrayList<>();
        //when winning the cards go to this pile
        int count1 = 0;
        int count2 = 0;
        while(fight){
            round++;
            if(round > 100){
                if(count1>count2) return 1;
                if(count2>count1) return 2;
                if(count1==count2) return 3;
            }
            if(deck1.size() > deck2.size()) count1++;
            else if(deck2.size() > deck1.size()) count2++;
            System.out.println("rounds: " +round + "Player1: "+deck1.size()+" Player2: "+deck2.size());

            Fight(deck1,deck2,holder1,holder2);
            if(deck1.isEmpty()){
                if(holder1.isEmpty()){
                    fight = false;
                }
                else{
                    deck1 = new ArrayList<String>(holder1);
                    holder1 = new ArrayList<String>();

                }
            }
            if(deck2.isEmpty()){
                if(holder2.isEmpty()){
                    fight = false;
                }
                else{
                    deck2 = new ArrayList<String>(holder2);
                    holder2 = new ArrayList<String>();
                }
            }
        }

        if(deck1.size() >0){
            return 1;
        }
        else if(deck2.size() >0){
            return 2;
        }
        else{
            return 3;
        }
    }

    private static void Fight(List<String>deck1, List<String>deck2, List<String>holder1, List<String>holder2){
            String card1 = deck1.remove(deck1.size()-1);
            String card2 = deck2.remove(deck2.size()-1);
            int val1;
            int val2;
            if(card1.length() == 2)
                val1 = order.indexOf(card1.substring(0,1));
            else
                val1 = order.indexOf(card1.substring(0,2));
            if(card2.length() == 2)
                val2 = order.indexOf(card2.substring(0,1));
            else
                val2 = order.indexOf(card2.substring(0,2));

            if(val1>val2){
                holder1.add(card1);
                holder1.add(card2);
            }
            else if(val2>val1){
                holder2.add(card1);
                holder2.add(card2);
            }
            else{
                War(deck1,deck2, holder1, holder2, 0);
            }
    }
    private static void War(List<String>deck1, List<String>deck2, List<String>holder1, List<String>holder2, int count){
        System.out.println("1,2,3 FLIP!");
        try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        String card ="";
        int val1;
        int val2;
        int i;
        if(deck1.size() <(4+count) && deck2.size() <(4+count)){
            System.out.println("WAR3");

            return;
        }
        else if(deck1.size() <(4+count)){
            System.out.println("WAR2");
            // 2 wins

            for(i = 0; i<deck1.size(); i++){
                holder2.add(deck1.get(deck1.size()-1-i));
            }
            deck1 = new ArrayList<String>();
            for(i = 0; i<count+4; i++){
                card = deck2.remove(deck2.size()-1);
                holder2.add(card);
            }
            return ;

        }
        else if(deck2.size() <(4+count)){
            System.out.println("WAR1");

            for(i = 0; i<count+4; i++){
                card = deck1.remove(deck1.size()-1);
                holder1.add(card);
            }
            for(i = 0; i<deck2.size(); i++){
                holder1.add(deck2.get(deck2.size()-1-i));
            }
            deck2 = new ArrayList<String>();
            return ;
        }
        String card1 = deck1.get(deck1.size()-4-count);
        String card2 = deck2.get(deck2.size()-4-count);

        if(card1.length() == 2)
            val1 = order.indexOf(card1.substring(0,1));
        else
            val1 = order.indexOf(card1.substring(0,2));
        if(card2.length() == 2)
            val2 = order.indexOf(card2.substring(0,1));
        else
            val2 = order.indexOf(card2.substring(0,2));

        if(val1>val2){
            System.out.println("WAR1");

            for(i = 0; i<count+4; i++){
                card = deck1.remove(deck1.size()-1);
                holder1.add(card);
            }
            for(i = 0; i<count+4; i++){
                card = deck2.remove(deck2.size()-1);
                holder1.add(card);
            }
        }
        else if(val2>val1){
            System.out.println("WAR2");

            for(i = 0; i<count+4; i++){
                card = deck1.remove(deck1.size()-1);
                holder2.add(card);
            }
            for(i = 0; i<count+4; i++){
                card = deck2.remove(deck2.size()-1);
                holder2.add(card);
            }
        }
        else{
            System.out.println("WAR4");

            War(deck1,deck2, holder1, holder2, count+4);
        }
    }

    public static void main(String[]args){
        // Build deck
        FillDeck();
        List<String>deck1 = new ArrayList<>();
        List<String>deck2 = new ArrayList<>();
        Dealer(deck1,deck2);

        // Play hands
        // Winner declared
        System.out.println("Winner: "+Play(deck1,deck2));
    }
}
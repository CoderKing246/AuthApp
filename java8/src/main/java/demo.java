//class Engine{
//    void start(){
//        System.out.println("start");
//    }
//}

class Car {
    private String model;
    private boolean isEngineOn;

    // this is a member to this Car calss
    class Engine{
        void start(){
            if(!isEngineOn){
                isEngineOn = true;
                System.out.println("engine on");
            }
            else {
                System.out.println("Already on");
            }
        }
    }
}
public class demo {
    static void main(String[] args) {

    }
}

package cl.fosforos.trazabilidadtemc;

/**
 * Created by abustamante on 27-09-2016.
 */

public class Utilies {
    private static Utilies instance;
    public int swMenu;

    public Utilies() {
    }

    public int getSwMenu() {
        return swMenu;
    }

    public void setSwMenu(int swMenu) {
        this.swMenu = swMenu;
    }

    public static synchronized Utilies getInstance(){
     if (instance==null){
         instance=new Utilies();
     }
        return instance;
    }
}

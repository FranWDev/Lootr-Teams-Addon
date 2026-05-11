import java.lang.reflect.Method;
import noobanidus.mods.lootr.data.ChestData;

public class PrintMethods {
    public static void main(String[] args) {
        for (Method m : ChestData.class.getDeclaredMethods()) {
            System.out.println(m.getName());
            for (Class<?> param : m.getParameterTypes()) {
                System.out.println("  " + param.getName());
            }
        }
    }
}

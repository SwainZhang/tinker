package FiexUtils;

import android.content.Context;

import com.emery.test.tinker.MyConstants;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by MyPC on 2016/12/16.
 */

public class FixUtil {
    private static HashSet<File> dexSet = new HashSet<>();
    static {
        dexSet.clear();
    }

    public static void loadDexFile(Context context) {
        if (context == null) {
           return;
        }

        File dir = context.getDir(MyConstants.DEX_DIR, Context.MODE_PRIVATE);
        File[] fixedDexFiles = dir.listFiles();
        for (File fixedDexFile : fixedDexFiles) {
            if (fixedDexFile.getName().startsWith("class") && fixedDexFile.getName().endsWith("" +
                    ".dex")) {
                dexSet.add(fixedDexFile);//存入集合；
            }
        }
        //合并dex
        mergeDexs(context,dir,dexSet);

    }

    private static void mergeDexs(Context appContext, File dir, HashSet<File> dexFiles) {

        String optimizedDirectory = dir.getAbsolutePath() + File.separator + "opt_dex"; //类加载器加载的目录

        File fdir = new File(optimizedDirectory);
        if (!fdir.exists()) {
            fdir.mkdir();
        }

        //1.PathClassLoader加载应用程序的dex
        PathClassLoader pathClassLoader = (PathClassLoader) appContext.getClassLoader();

        for (File dex : dexFiles) {
            //2.DexClassLoader加载用来修复的dex
            DexClassLoader dexClassLoader = new DexClassLoader(
                    dex.getAbsolutePath(), //dexPath
                    fdir.getAbsolutePath(), //optimizedDirectory 加载完后进行优化的文件夹
                    null,                   //libraryPath 与你要加载dex相关的so库
                    pathClassLoader);      //classLoaderParent 应用程序的classLoader

           // 加载一个合并一个

            try {
                //1.通过BaseDexClassLoader 拿到 DexPathList dexPathList

                Object dexPathListOld=  getDexPathList(pathClassLoader);
                Object dexPathListMine  =  getDexPathList(dexClassLoader);

                //2.通过DexPathList 拿到其成员变量 Element[] dexElements
                Object mineElementArray = getElementArray(dexPathListMine);
                Object oldElementArray=getElementArray(dexPathListOld);

                //3.两个Element[] dexElements合并,将新的dexElements放前面
                Object newDexElements = combineArray(mineElementArray, oldElementArray);

                //4.用新的 Element[] dexElements 替换原来的DexPathList里的 dexElements
                setField(dexPathListOld,dexPathListOld.getClass(),"dexElements",newDexElements);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }


    }

    /**
     * 合并两个dexElements  [左数组+右数组]
     * @param arrayLhs  左数组对象
     * @param arrayRhs  右数组对象
     * @return
     */
    private static Object combineArray(Object arrayLhs,Object arrayRhs){
        //先拿到字节码，再看类型是不是数组
        Class clazz = arrayLhs.getClass().getComponentType();//通过dexElements拿到对象类型是数组 Element[]
        int lenLhs=Array.getLength(arrayLhs);
        int length=lenLhs+Array.getLength(arrayRhs);

        Object newDexElements = Array.newInstance(clazz, length);//新的合并的数组
        for(int i=0;i<length;i++){
            if(i<lenLhs) {
                Array.set(newDexElements, i, Array.get(arrayLhs, i));//左数组放前面
            }else{
                Array.set(newDexElements,i,Array.get(arrayRhs,i-lenLhs));//接着左数组
            }
        }

        return  newDexElements;
    }
    /**
     * 通过DexPathList拿到Element[] dexElements
     * @param obj
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */

    private static Object getElementArray(Object obj) throws ClassNotFoundException, NoSuchFieldException,
            IllegalAccessException {
        return  getField(obj,Class.forName("dalvik.system.DexPathList"),"dexElements");
    }



    /**
     * 通过BaseDexClassLoader拿到dexPathList
     * @param baseClassLoader
     * @return  dexPathLoader
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private  static Object getDexPathList(Object baseClassLoader) throws ClassNotFoundException,
            NoSuchFieldException, IllegalAccessException {
       return      getField(baseClassLoader,Class.forName("dalvik.system.BaseDexClassLoader"),"pathList");

    }


    /**
     * 通过反射拿到特定成员变量
     * @param obj 类的对象
     * @param clazz   类的字节码
     * @param fieldName 成员变量名字
     */
    private  static Object getField(Object obj, Class<?> clazz, String fieldName) throws
            IllegalAccessException, NoSuchFieldException {

            Field specifyField = clazz.getDeclaredField(fieldName);
            specifyField.setAccessible(true);//可能是private

        return  specifyField.get(obj);
    }


    /**
     * 通过反射设置特定成员变量
     * @param obj 类的对象
     * @param clazz   类的字节码
     * @param fieldName 成员变量名字
     */
    private  static void  setField(Object obj, Class<?> clazz, String fieldName,Object value) throws
            IllegalAccessException, NoSuchFieldException {

        Field specifyField = clazz.getDeclaredField(fieldName);
        specifyField.setAccessible(true);//可能是private
        specifyField.set(obj,value);
    }
}




import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;


public class Index implements Serializable {

    private static final long serialVersionUID = 4985640899456326696L;

    private final TreeMap<String, String> tracking;//将文件名映射到文件最新版本的哈希值的treemap


    public Index(TreeMap<String, String> tracking) {//index 构造函数

        this.tracking = tracking;

    }


    /**
     * 创建当前工作区快照构成的treemap并将其写入文件
     * 它首先读取index文件，如果文件为空，则创建一个新的Index对象。否则，反序列化文件的内容以获取Index对象。
     * 然后，遍历传入的文件列表，读取每个文件的内容，并计算内容的哈希值，创建一个包含文件名和哈希值的键值对，并将其添加到跟踪对象中。
     * 最后，它使用ObjectOutputStream将index_object写入一个新的索引文件中。
     *
     * @param files 路径对象列表
     * @throws Exception
     */

    public static void writeIndex(List<Path> files) throws Exception {


        //读取index文件。 如果文件为空，创建一个新的 Index 对象。否则，反序列化文件的内容以获取 Index 对象
        Index index_object;

        TreeMap<String, String> tracking = new TreeMap<>();

        //如果index为空，新建对象

        if (utility.readFile(utility.getIndexFilePath().toString()).equals("")) {

            index_object = new Index(tracking);

        } else {//index不为空，读原先的index内容，新增entry

            index_object = Index.deSerialFrom();

            if (index_object != null) {

                tracking = index_object.getTracking();

            }

        }
        //遍历 Path 对象的输入列表，读取每个路径中的文件内容，并计算内容的哈希值，创建一个包含文件名和哈希值的键值对 ，并将其放入跟踪对象中。

        ObjectOutputStream oos = null;

        try {

            for (Path file : files) {

                if (!new File(file.toString()).isHidden()) {
                    String file_name=file.toAbsolutePath().toString().substring(utility.getPath().toString().length());
                    //String file_name = file.toString();

                    String content = utility.readFile(file.toString());

                    String hash = utility.str2Hash(content);

                    tracking.put(file_name, hash);

                }
            }

            //创建一个新index文件，并使用 ObjectOutputStream 将 index_object 写入其中。
            File index_file = new File(utility.getIndexFilePath().toString());
            oos = new ObjectOutputStream(new FileOutputStream(index_file));
            oos.writeObject(index_object);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {//关闭对象输出流
                oos.close();
            }
        }
    }

    /**
     * 用于从位于指定路径的文件中反序列化 index 对象。
     * 该方法使用 ObjectInputStream 从文件中读取序列化数据，然后调用 readObject() 方法将其反序列化为 index对象。
     * 该方法同时捕获 IOException 和 ClassNotFoundException 并通过打印堆栈跟踪并在出现任何异常时返回 null 来处理它们。
     *
     * @return index对象
     */

    public static Index deSerialFrom() {

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(utility.getIndexFilePath().toString()));
            return (Index) in.readObject();

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    //该方法返回index的 TreeMap 属性
    public TreeMap<String, String> getTracking() {

        return tracking;

    }


}


import java.io.*;
import java.nio.file.Path;
import java.util.List;

class Blob implements Serializable {
    private static final long serialVersionUID = 2732203542813883744L;

    private final String type;//对象类型

    private final int size;//内容字符数

    private final String content;//文件内容


    public Blob(String content) {//构造函数
        this.type = "blob";
        this.size = content.length();
        this.content = content;
    }

    /**
     * @param blob blob对象
     * @return blob对象的所在路径
     */
    public static String get_Blob_path(Blob blob) {
        return utility.getFilesPath().resolve(utility.str2Hash(blob.content)).toString();
    }

    /**
     * 在objects文件夹中保存blob对象
     * 读取文件内容，转变为hash值
     * 在objects中添加名为hash值，内容为blob+" "+内容字符数+ 内容的文件
     */

    public static void writeBlob(List<Path> files) throws IOException {
        ObjectOutputStream oos = null;
        try {
            for (Path file : files) {
                if (!new File(file.toString()).isHidden()) {
                    String content = utility.readFile(file.toString());
                    if (content != null) {
                        Blob blob = new Blob(content);
                        File f1 = new File(get_Blob_path(blob));
                        oos = new ObjectOutputStream(new FileOutputStream(f1));
                        oos.writeObject(blob);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (oos != null) {
                oos.close();
            }
        }
    }

    /**
     * 用于从位于指定路径的文件中反序列化 blob 对象。
     * 该方法使用 ObjectInputStream 从文件中读取序列化数据，然后调用 readObject() 方法将其反序列化为 blob对象。
     * 该方法同时捕获 IOException 和 ClassNotFoundException 并通过打印堆栈跟踪并在出现任何异常时返回 null 来处理它们。
     *
     * @return blob对象
     */
    public static Blob deSerialFrom(String path) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
            return (Blob) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * @return blob保存的文件内容
     */
    public String getContent() {
        return content;
    }

    /**
     * @return blob对象内容
     */
    public String toString() {
        return type + " " + size + " " + content;
    }


}

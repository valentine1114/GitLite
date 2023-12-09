## Git Lite: A Version-Control System
Welcome to the GitHub repository for Git Lite, a streamlined version-control system developed using Java and principles of Object-Oriented Programming. This project,  focuses on delivering essential functionalities of a version-control system with an emphasis on efficiency, security, and user-friendly operations.

### Project Overview
Git Lite is designed as a simplified yet powerful tool for version control, providing key features necessary for effective management of file and folder changes. It stands as an ideal solution for those seeking a lightweight, yet functional alternative to complex version-control systems.

### Key Features
#### Change Tracking
File/Folder Change Detection: Efficiently tracks modifications in files and folders, ensuring comprehensive version control.
#### Version Management
Version Reverting: Enables users to revert to previous versions of their work with ease, enhancing workflow flexibility.
Branch Management: Supports creation, merging, switching, and deletion of branches, allowing for parallel development and version handling.
#### Efficient Data Handling
Hash Maps and Graph Traversals: Employed for efficient data search and commit tracking, optimizing the performance of the version-control system.
Commit Tree Structure: Designed for enhanced system flexibility, accommodating independent file directories and contents.
#### Security and Integrity
Serialization: Utilized for secure file storage, ensuring data integrity.
SHA-1 Hashes: Implements SHA-1 hashing for secure and reliable file identification.
### Technologies Used
Core Language: Java
Concepts: Object-Oriented Programming, Data Structures (Hash Maps, Trees)
Security: SHA-1 Hashing
Data Storage: Serialization

### 内部原理

- Git是主类
- Blob对象是对一个文件的抽象，保存着文件快照
- index文件保存暂存区信息，数据结构是将文件名（相对于git仓库主文件夹的相对路径）映射到文件的hash的Map
- tree 文件根据某一时刻暂存区（即 index 区域）所表示的状态创建并记录一个对应的树对象
- commit保存上一次commit ID和此次提交的tree ID，并将本次commit ID保存至refs文件夹中的对应分支名称文件
- 每次我们运行 git add 和 git commit 命令时，Git 所做的工作实质就是将被改写的文件保存为数据对象， 更新暂存区，记录树对象，最后创建一个指明了树对象和父提交的提交对象。

### 设计框架

  -.Mygit 存储一切
    -objects 存储commit、tree和blob对象（使用hashcode作为文件名）
     	-commits 文件夹存储每个commit对象
-commit记录用户每次commit的msg，时间信息，并对相关文件进行跟踪。每个commit实例都是commit tree上的一个节点。通过parent属性追踪上一个commit，通过head属性追踪其分支名称
     -存储每个blob和tree对象

- blob 将每个文件的哈希值和content以blob的形式存起来
- tree 存储此次提交commit的index文件快照
      -refs
       	-heads 存储分支末端（文件名为分支名，内容为对应commit的hashCode）
     - HEAD 存储当前commit 所在的分支名称
       - index 存储缓存区内容（文件名-文件最新版本的hashCode）

![image.png](https://cdn.nlark.com/yuque/0/2023/png/32665762/1673527305413-5a08c06a-7cf0-4f5c-8885-47d7f81b87dc.png#averageHue=%23efefef&clientId=u3cea3ec8-207f-4&from=paste&height=815&id=ufcbb2de0&originHeight=815&originWidth=962&originalType=binary&ratio=1&rotation=0&showTitle=false&size=99143&status=done&style=none&taskId=ucbfd0bd9-14e4-423d-ba6c-045db66e0ec&title=&width=962)

### 项目亮点： 

这个项目是一个简单的版本控制系统的实现，亮点包括：

1. 分支管理功能: 支持了创建、切换、删除分支等操作。
2. 实现了文件夹操作: 支持对文件夹进行提交、删除等操作。
3. 支持绝大多数的版本控制功能: 如commit、checkout、status、diff、find，push/pull等功能。如：
   1.  commit功能：
       - 使用hash值维护每次的commit，可以保证每次commit的唯一性
       - 支持多级文件夹的commit，并能对工作区和缓存区进行区分
       - 支持对commit进行查询，能快速找到指定commit。
       - 支持对commit进行分支，能在不同版本上进行操作
       - 支持对commit进行回滚，能在不同版本之间切换。
   2.  rm 功能：
       - 支持删除文件和文件夹，并能够在删除文件时对比工作区和缓存区文件的内容，确保删除的文件是最新的版本。
       - 支持使用 "--f" 和 "--cached" 选项强制删除文件，这样可以在文件已经被修改或者已经被添加到缓存区的情况下强制删除文件。
       - 在删除文件时，提供了足够的异常处理，如文件不存在，文件未被缓存等，能够有效地提示用户错误信息。
       - 支持递归删除文件夹，能够删除文件夹内的所有文件。
   3.  push/pull可以将本地版本库的内容与远程版本库的内容进行同步，大大简化团队协作的流程，提高协作效率。
4. 完成了额外的操作，如reflog/status/diff/分支管理 /find /checkout等功能。
5. 熟练运用IO流，使用了FileWriter、ObjectOutputStream等IO流来实现对文件的读写操作，方便地实现了读写文件的操作。
6. 在每个操作函数中都进行了相应的异常处理，可以很好地保证程序在出现错误时继续执行并给出相应的提示信息。


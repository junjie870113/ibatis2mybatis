# iBatis2Mybatis转换工具

## 重要信息

基于dtd定义的抽象语法树(AST)进行逻辑完备的转换

有任何问题,请邮件咨询junjie870113@gmail.com

## 概述

+ iBatis xml语法文件转换 (xml-converter)

## 运行

运行时需指定目标工程目录作为入参，参考如下命令示例(JDK8环境)

#### production模式

直接对指定目录下的文件进行修改，请做好文件保存工作

```shell
    mvn clean package

    java -jar xml-1.0-jar-with-dependencies.jar ${target project root}

```

#### sandbox模式

对指定目录进行与扫描，检查不支持的项目(仅xml语法文件转换程序支持预查)

```shell
    mvn clean package

    java -jar xml-1.0-jar-with-dependencies.jar ${target project root} sandbox

```

console 输出内容的含义:

+ Unsupported attribute below(maybe empty) --- \${标签} \${不支持的属性}

  请在原始工程中通过IDE自行根据关键词找到对应的语法文件

+ all xml elements in target project --- 目标工程所有xml标签


## TODO List

1. ibatis2mybatis 工具中对于ibatis dtd的语义转换，部分属性尚未找到mybatis的对应写法

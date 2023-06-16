# iBatis2Mybatis converter

convert base on AST defined by ibatis DTD

if you have any question, plz feel free to contact with me(junjie870113@gmail.com)

## summary

+ this tool helps you convert ibatis sql map files to mybatis sql map files

## requirement

JDK8+

#### running mode

#### production

Since this tool will convert the files directly, backup your projects before start.

```shell
    mvn clean package

    java -jar xml-1.0-jar-with-dependencies.jar ${target project root}

```

#### sandbox

instead of changing anything, this mode will pre-check the unsupported attribute & elements in your projects
it's a good idea to change those unsupported things manually. ^_^

```shell
    mvn clean package

    java -jar xml-1.0-jar-with-dependencies.jar ${target project root} sandbox

```

meaning of the output:

+ Unsupported attribute below(maybe empty) --- \${label} \${unsupported attribute}

+ all xml elements in target project

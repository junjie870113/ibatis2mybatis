package indi.lujunjie.mybatis.xmlgramma.context;

public interface Removable {

    <T> T removeSelf(boolean removeSelf);

    boolean removeSelf();

    <T> T removeRecursive(boolean removeRecursive);

    boolean removeRecursive();
}

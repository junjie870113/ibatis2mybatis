package indi.lujunjie.mybatis.xmlgramma;

import indi.lujunjie.mybatis.xmlgramma.processor.Processor;
import indi.lujunjie.mybatis.xmlgramma.processor.ProductionProcessor;
import indi.lujunjie.mybatis.xmlgramma.processor.SandboxProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Lu Jun Jie
 * @date 2021-09-22 13:58
 */
public class Application {

    public static void main(String[] args) {
        Map<String, Processor> cache = new HashMap<>();
        ServiceLoader.load(Processor.class).forEach(item -> cache.put(item.name(), item));
        String mode = args.length > 1 && SandboxProcessor.MODE.equalsIgnoreCase(args[1]) ?
                SandboxProcessor.MODE : ProductionProcessor.MODE;
        cache.get(mode).process(args[0]);
    }
}

## 注意添加assets/xpose_init

## 注意jar包的引入方式


    provided fileTree(dir: 'deps', include: ['*.jar'])
    provided files('lib/XposedBridgeApi-87.jar')
    
## 软重启 
    

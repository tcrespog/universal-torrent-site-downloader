import ch.qos.logback.classic.Level
import java.lang.reflect.Modifier

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = '%date{yyyy-MM-dd HH:mm:ss} %-5level %logger - %message%n'
    }
}

List<Level> allLevels = Level.class.declaredFields.toList().findResults {
    (it.type == Level.class) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && Modifier.isPublic(it.modifiers) ? it.get(null) : null
}
Level logLevel = System.getProperty('logLevel') in allLevels*.toString() ? Level."${System.getProperty('logLevel')}" : INFO
root(logLevel, ['STDOUT'])
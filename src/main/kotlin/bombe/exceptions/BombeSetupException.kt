package bombe.exceptions

inline fun bombeCheck(value: Boolean, componentName: String, errorCode: String, subCode: String, message: String): Unit {
    if (!value) {
        throw BombeSetupException(componentName, errorCode, subCode, message)
    }
}

//inline fun bombeCheck(value: Boolean, lazyComponentName: () -> String, lazyErrorCode: () -> String, lazySubCode: () -> String, lazyMessage: () -> String): Unit {
//    if (!value) {
//        val componentName = lazyComponentName()
//        val errorCode = lazyErrorCode()
//        val subCode = lazySubCode()
//        val message = lazyMessage()
//        throw BombeSetupException(componentName, errorCode, subCode, message)
//    }
//}

class BombeSetupException(val componentName:String, val errorCode:String, val subCode:String, override val message:String) : RuntimeException() {
    override fun toString(): String {
        return "BombeSetupException: [$componentName][$errorCode-$subCode] $message"
    }
}
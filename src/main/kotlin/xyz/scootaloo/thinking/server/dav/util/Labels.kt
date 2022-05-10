package xyz.scootaloo.thinking.server.dav.util

/**
 * @author flutterdash@qq.com
 * @since 2022/5/7 14:00
 */

open class DAVCommonLabels {
    val href = "href"
    val depth = "Depth"
    val multiStatus = "multiStatus"
}

/**
 * ```json
 * {
 *     "LockType": "write",
 *     "LockScope": "Shared",
 *     "Owner": "Username"
 * }
 * ```
 */
object DAVCommonLockLabels {
    const val lockScope = "LockScope"
    const val lockType = "LockType"
    const val shared = "shared"
    const val exclusive = "exclusive"
}

/**
 * ```json
 * {
 *     "propName": bool
 *     "props": [{
 *     "url": string,
 *     "props": [string]
 *    }]
 * }
 * ```
 */
object DAVPropFindLabels {
    const val prop = "prop"
    const val propName = "propName"
    const val allProp = "allProp"
    const val url = "url"
    const val props = "props"
}
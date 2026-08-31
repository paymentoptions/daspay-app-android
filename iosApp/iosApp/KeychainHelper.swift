import Foundation
import Security

@objc public class KeychainHelper: NSObject {

    @objc public static func get(_ key: String) -> String? {

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: "com.paymentoptions.pos",
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var result: AnyObject?

        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess,
              let data = result as? Data,
              let value = String(data: data, encoding: .utf8) else {
            return nil
        }

        return value
    }

    @objc public static func put(_ key: String, value: String) {

        let data = value.data(using: .utf8)!

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: "com.paymentoptions.pos",
            kSecAttrAccount as String: key
        ]

        SecItemDelete(query as CFDictionary)

        var add = query

        add[kSecValueData as String] = data
        add[kSecAttrAccessible as String] =
            kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly

        SecItemAdd(add as CFDictionary, nil)
    }
}
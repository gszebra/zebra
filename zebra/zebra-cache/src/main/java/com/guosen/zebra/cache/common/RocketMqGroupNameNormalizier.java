package com.guosen.zebra.cache.common;

/**
 * Rocket MQ组名规范器
 */
public final class RocketMqGroupNameNormalizier {

    private RocketMqGroupNameNormalizier(){}

    /**
     * 对Rocket MQ组名进行规范化
     * @param groupName 组名
     * @return  规范化后的组名
     */
    public static String normalize(String groupName) {
        char[] originalChars = groupName.toCharArray();
        char[] newChars = new char[originalChars.length];

        for (int i = 0; i < originalChars.length; i++) {
            char originalChar = originalChars[i];

            // 如果不是合法的组名字符，那么用'-'替代
            char newChar = isLegalChar(originalChar) ? originalChar : '-';
            newChars[i] = newChar;
        }

        return new String(newChars);
    }

    private static boolean isLegalChar(char ch) {

        // rocket mq的group只支持%|a-zA-Z0-9_-这些符号
        boolean legal = true;
        do {
            if ('a' <= ch && ch <= 'z') {
                break;
            }
            if ('A' <= ch && ch <= 'Z') {
                break;
            }
            if ('0' <= ch && ch <= '9') {
                break;
            }
            if (ch == '%') {
                break;
            }
            if (ch == '|') {
                break;
            }
            if (ch == '_') {
                break;
            }
            if (ch == '-') {
                break;
            }

            legal = false;
        } while (false);

        return legal;
    }
}

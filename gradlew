#!/usr/bin/env bash

# Gradle Wrapper 启动脚本（Unix）
# 这个文件需要在有 Gradle 环境的系统上使用

set -e

# 设置项目根目录
APP_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# 检查 JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    echo "错误: JAVA_HOME 环境变量未设置"
    exit 1
fi

# 执行 Gradle 命令
exec "$JAVA_HOME/bin/java" \
    -classpath gradle/wrapper/gradle-wrapper.jar \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
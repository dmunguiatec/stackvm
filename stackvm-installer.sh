#!/usr/bin/env bash

set -euo pipefail

APP_NAME="stackvm"
INSTALL_DIR="/opt/$APP_NAME"
BIN_LINK="/usr/local/bin/$APP_NAME"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUNDLE_PATH="$SCRIPT_DIR/vm/app/build/install/$APP_NAME"

usage() {
    echo "Usage: sudo $0 {install|uninstall}"
    exit 1
}

install_bundle() {
    if [[ ! -d "$BUNDLE_PATH" ]]; then
        echo "Error: bundle not found at $BUNDLE_PATH"
        echo "Run 'cd vm && ./gradlew clean :app:generateGrammarSource installDist && cd ..' first."
        exit 1
    fi

    echo "Installing $APP_NAME from $BUNDLE_PATH..."

    rm -rf "$INSTALL_DIR"
    cp -r "$BUNDLE_PATH" "$INSTALL_DIR"

    chmod +x "$INSTALL_DIR/bin/$APP_NAME"
    ln -sf "$INSTALL_DIR/bin/$APP_NAME" "$BIN_LINK"

    echo "Installed successfully."
    echo "Run: $APP_NAME --help"
}

uninstall_bundle() {
    echo "Uninstalling $APP_NAME..."

    rm -f "$BIN_LINK"
    rm -rf "$INSTALL_DIR"

    echo "Uninstalled successfully."
}

main() {
    case "${1:-}" in
        install)
            install_bundle
            ;;
        uninstall)
            uninstall_bundle
            ;;
        *)
            usage
            ;;
    esac
}

main "$@"
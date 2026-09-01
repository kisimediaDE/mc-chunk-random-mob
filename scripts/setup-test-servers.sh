#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
SERVERS_DIR="$PROJECT_DIR/test-servers"
PLUGIN_JAR="$PROJECT_DIR/build/libs/chunk-mob-challenge-1.0.0.jar"
USER_AGENT="chunk-mob-challenge-test/1.0 (https://github.com/playmonkeei)"

PAPER_261_URL="https://fill-data.papermc.io/v1/objects/1d70b1dab9cf4a6de615209a536f3a45a2186240253c428213ce2188ab95e5f7/paper-26.1.2-74.jar"
PAPER_261_SHA="1d70b1dab9cf4a6de615209a536f3a45a2186240253c428213ce2188ab95e5f7"
PAPER_262_URL="https://fill-data.papermc.io/v1/objects/0de30efb024bc8b83c9c7d507d11802897ad8056b6110ec09fe1a91d126ccb54/paper-26.2-121.jar"
PAPER_262_SHA="0de30efb024bc8b83c9c7d507d11802897ad8056b6110ec09fe1a91d126ccb54"

download_paper() {
    version=$1
    url=$2
    expected_sha=$3
    directory="$SERVERS_DIR/$version"
    target="$directory/server.jar"

    mkdir -p "$directory/plugins"
    if [ -f "$target" ]; then
        actual_sha=$(shasum -a 256 "$target" | awk '{print $1}')
        if [ "$actual_sha" = "$expected_sha" ]; then
            echo "Paper $version ist bereits vorhanden und verifiziert."
            return
        fi
        echo "Paper $version hat eine falsche Prüfsumme und wird neu geladen."
    fi

    echo "Lade Paper $version herunter ..."
    curl --fail --location --header "User-Agent: $USER_AGENT" "$url" --output "$target.tmp"
    actual_sha=$(shasum -a 256 "$target.tmp" | awk '{print $1}')
    if [ "$actual_sha" != "$expected_sha" ]; then
        echo "FEHLER: SHA-256-Prüfung für Paper $version fehlgeschlagen." >&2
        exit 1
    fi
    mv "$target.tmp" "$target"
}

echo "Baue ChunkMobChallenge ..."
"$PROJECT_DIR/gradlew" clean build

download_paper "26.1.2" "$PAPER_261_URL" "$PAPER_261_SHA"
download_paper "26.2" "$PAPER_262_URL" "$PAPER_262_SHA"

cp "$PLUGIN_JAR" "$SERVERS_DIR/26.1.2/plugins/ChunkMobChallenge.jar"
cp "$PLUGIN_JAR" "$SERVERS_DIR/26.2/plugins/ChunkMobChallenge.jar"

echo
echo "Beide Testserver sind bereit:"
echo "  Paper 26.1.2: ./scripts/start-test-server.sh 26.1.2"
echo "  Paper 26.2:   ./scripts/start-test-server.sh 26.2"
echo "  Verbindung:   localhost:25565 (immer nur einen Server gleichzeitig starten)"

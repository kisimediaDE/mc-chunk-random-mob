#!/usr/bin/env sh
set -eu

if [ "$#" -ne 1 ]; then
    echo "Verwendung: $0 <26.1.2|26.2>" >&2
    exit 2
fi

case "$1" in
    26.1.2|26.2) VERSION=$1 ;;
    *)
        echo "Unbekannte Version '$1'. Erlaubt sind 26.1.2 und 26.2." >&2
        exit 2
        ;;
esac

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
SERVER_DIR="$PROJECT_DIR/test-servers/$VERSION"
SERVER_JAR="$SERVER_DIR/server.jar"
PLUGIN_JAR="$PROJECT_DIR/build/libs/chunk-mob-challenge-1.0.0.jar"

if [ ! -f "$SERVER_JAR" ]; then
    echo "Paper fehlt. Führe zuerst ./scripts/setup-test-servers.sh aus." >&2
    exit 1
fi

echo "Baue und synchronisiere die aktuelle Plugin-JAR ..."
"$PROJECT_DIR/gradlew" build
mkdir -p "$SERVER_DIR/plugins"
cp "$PLUGIN_JAR" "$SERVER_DIR/plugins/ChunkMobChallenge.jar"

echo
echo "Starte Paper $VERSION in $SERVER_DIR"
echo "Minecraft-Adresse: localhost:25565"
echo "Durch den Start mit -Dcom.mojang.eula.agree=true bestätigst du Mojangs EULA:"
echo "https://aka.ms/MinecraftEULA"
echo "Server sauber mit dem Konsolenbefehl 'stop' beenden – NICHT mit '/cc stop'."
echo

cd "$SERVER_DIR"
exec java -Dcom.mojang.eula.agree=true -Xms1G -Xmx2G -jar "$SERVER_JAR" --nogui

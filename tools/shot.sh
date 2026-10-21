#!/bin/bash
# Usage: shot.sh <nom_image> <délai_s> <commande> [entrées...]
# Exécute la commande dans une session tmux, tape les entrées éventuelles et capture le résultat.
NAME=$1; WAIT=$2; CMD=$3; shift 3
S=tp3shot
TOOLS=/home/user/Ramzi/tools
OUT=/home/user/Ramzi/rapport/img
DIR=${SHOT_DIR:-$PWD}
SHORT="~/TP3${DIR#/home/user/Ramzi}"
TITLE=${SHOT_TITLE:-"etudiant@isep: $SHORT"}
tmux kill-session -t $S 2>/dev/null
tmux new-session -d -s $S -x ${SHOT_COLS:-130} -y ${SHOT_ROWS:-50} -c "$DIR" \
  "env -u JAVA_TOOL_OPTIONS LC_ALL=C.UTF-8 HOME=/home/user/Ramzi PS1='\[\e[1;32m\]etudiant@isep\[\e[0m\]:\[\e[1;34m\]\w\[\e[0m\]\$ ' bash --norc -i"
sleep 0.5
tmux send-keys -t $S "clear" Enter; sleep 0.3
tmux send-keys -t $S "$CMD" Enter
for inp in "$@"; do sleep ${SHOT_INPUT_DELAY:-2}; tmux send-keys -t $S "$inp" Enter; done
sleep $WAIT
tmux capture-pane -t $S -e -p -S - -J > /tmp/claude-0/$NAME.ansi
tmux kill-session -t $S
python3 $TOOLS/term_render.py /tmp/claude-0/$NAME.ansi $OUT/$NAME.png "$TITLE"
echo "-> $OUT/$NAME.png"

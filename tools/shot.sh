#!/bin/bash
# Usage: shot.sh <nom_image> <délai_s> <commande> [entrées...]
# Exécute la commande dans une session tmux (style Terminal macOS), tape les entrées et capture le résultat.
NAME=$1; WAIT=$2; CMD=$3; shift 3
S=tp3shot
TOOLS=/home/user/Ramzi/tools
OUT=/home/user/Ramzi/rapport/img
U=ramziguenoune
DIR=${SHOT_DIR:-$PWD}
COLS=${SHOT_COLS:-130}; ROWS=${SHOT_ROWS:-50}
TITLE=${SHOT_TITLE:-"$(basename "$DIR") — -zsh — ${COLS}×${ROWS}"}
tmux kill-session -t $S 2>/dev/null
tmux new-session -d -s $S -x $COLS -y $ROWS -c "$DIR" \
  "sudo -u $U env -i PATH=$PATH:/opt/maven/bin TERM=xterm-256color LC_ALL=C.UTF-8 HOME=/Users/$U USER=$U PGPASSWORD=postgres PS1='$U@MacBook-Pro-de-Ramzi \W % ' bash --norc -i"
sleep 0.7
tmux send-keys -t $S "clear" Enter; sleep 0.3
tmux send-keys -t $S "$CMD" Enter
for inp in "$@"; do sleep ${SHOT_INPUT_DELAY:-2}; tmux send-keys -t $S "$inp" Enter; done
sleep $WAIT
tmux capture-pane -t $S -e -p -S - -J > /tmp/claude-0/$NAME.ansi
tmux kill-session -t $S
python3 $TOOLS/term_render.py /tmp/claude-0/$NAME.ansi $OUT/$NAME.png "$TITLE"
echo "-> $OUT/$NAME.png"

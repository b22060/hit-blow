SSH_USER = isdev24
SSH_HOST = 150.89.233.203
REMOTE_DIR = hit-blow

.PHONY: run

#run:
#ssh $(SSH_USER)@$(SSH_HOST) \
		"cd $(REMOTE_DIR) && bash ./gradlew bootrun"

#180秒後に実行を停止 \
上記のようにしないと[80番ポートは使用中]というエラーが出る
run:
	ssh $(SSH_USER)@$(SSH_HOST) \
		"cd $(REMOTE_DIR) && bash ./gradlew bootrun & sleep 180 && pkill -f 'gradlew bootrun'"

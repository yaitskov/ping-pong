#!/bin/bash

. validate.sh

CERT_FILE=${cert:-domain-crt.txt}
KEY_FILE=${key:-domain-key.txt}

if [ -d "$1" ] ; then
    pushd "$1";
fi

trap "echo argument is \$1" EXIT

cat <<EOF > /tmp/setup-certs.sh
#!/bin/bash
BACKUP_DIR=/etc/nginx/backup-certs-$(date +%s)
cp -r /etc/nginx/certs \$BACKUP_DIR || { echo "backup creation failed"; exit 1; }
pushd /etc/nginx/certs
cp /home/${CLOUD_SPORT_ACCOUNT%%@*}/$CERT_FILE cloud-sport.pl.cer
cp /home/${CLOUD_SPORT_ACCOUNT%%@*}/$KEY_FILE cloud-sport.pl.key
service nginx reload
EOF

chmod +x /tmp/setup-certs.sh

scp /tmp/setup-certs.sh $KEY_FILE $CERT_FILE $CLOUD_SPORT_ACCOUNT:~

ssh $CLOUD_SPORT_ACCOUNT sudo bash -x ./setup-certs.sh

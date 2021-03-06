#!/usr/bin/env bash

JAR_NAME="DERAM-1.0.jar"

# Create deployment folder if not exist
if ! [[ -d deployment/ ]]
then
  mkdir deployment/
fi

 # Get JAR
 cd ../approaches
 ./build.sh --shared --deram
 cd -

 cp ../approaches/deram/target/${JAR_NAME} deployment/
 cp -r ../data deployment/
 cp evaluation.conf deployment/

# Get data-script
cp ../tests/bin/helper/create_dataset.sh deployment/

# Get experiments
cp experiments/*.sh deployment/

cd ansible
ansible-playbook --ask-pass deram-deployment.yaml --extra-vars "target=all"

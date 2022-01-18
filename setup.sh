echo "Creating the jar folder."
kubectl exec -it pulsar-toolset-0 -n pulsar -- mkdir -p connectors

echo "Uploading resources on the cluster ..."
kubectl cp jars/pulsar-io-activemq-2.7.4.nar pulsar-toolset-0:/pulsar/connectors/ -n pulsar
kubectl cp config.yaml pulsar-toolset-0:/pulsar/ -n pulsar

kubectl exec -it pulsar-toolset-0 -n pulsar -- bin/pulsar-admin source create \
                                                     --source-config-file config.yaml \
                                                     --name amqp-source --archive connectors/pulsar-io-activemq-2.7.4.nar

kubectl exec -it pulsar-toolset-0 -n pulsar -- bin/pulsar-admin sources list
kubectl exec -it pulsar-toolset-0 -n pulsar -- bin/pulsar-admin sources get --name amqp-source --tenant public --namespace default
kubectl exec -it pulsar-toolset-0 -n pulsar -- bin/pulsar-admin sources status --name amqp-source --tenant public --namespace default


#
#kubectl exec -it pulsar-toolset-0 -n pulsar -- bash
# bin/pulsar-admin topics list public/default
#bin/pulsar-admin source create \
#      --source-config-file config.yaml \
#      --name amqp-source --archive connectors/pulsar-io-activemq-2.9.1.nar

#bin/pulsar-admin sources list
#
#bin/pulsar-admin sources get --name amqp-source --tenant public --namespace default
#bin/pulsar-admin sources status --name amqp-source --tenant public --namespace default
#
#bin/pulsar-client consume -s sub amqp_topic -n 0
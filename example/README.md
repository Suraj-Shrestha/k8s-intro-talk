# Kubernetes Intro - examples

Here is the examples that are shown briefly in the presentation. Feel free to play around with it, and follow the guide below if you are completely new to Kubernetes :)


## About the database config
In this example the database is just a single pod in our cluster (or a container if you simply use docker-compose). This is not reliable as pods may suddenly be restarted. Without any data replication (which we don't have here for simplicity), the data would be lost. For MongoDB, data replication would be quite easy as the database is simply a file. So in theory, all that is needed is to use a volume. A lot of people deploying to Kubernetes clusters instead choose not to deploy the database in their cluster to avoid losing data (see description of `Endpoint` in the slides if you are curious on data outside the cluster). If you are curious about the challenges of running datastores in containers, you should [watch the talk "Challenges of running data stores in containers" by Johannes Unterstein from JavaZone 2018](https://2018.javazone.no/program/8f6ca5b7-2f94-47ea-a6d4-ed6a5ff6d88e).




## Installing a local Kubernetes cluster
Make sure you follow the guide to install your variant of a local Kubernetes cluster. There are several to choose from. Windows and Mac has Kubernetes built into their Docker installs, so that is an option. Another excellent option with many features is Minikube. To install Minikube, you can follow [the install guides at their website](https://minikube.sigs.k8s.io/docs/start/). Everything should work well with the Kubernetes cluster built into Docker Desktop and Docker for Mac as well. To enable those, right-click on the Docker icon in your system tray, choose settings, Kubernetes, then check the "Enable Kubernetes" checkbox. Now you should be able to follow along with the `kubectl` commands used here.


## Docker compose?
You may wonder why the `docker-compose.yml` file is included? Does it relate to Kubernetes in any way? While there are small similarities like the DNS entries created, it is not related to Kubernetes. It is kept here for you to see the differences (and another reason if you read the next paragraphs!). 


If you have built the image (by doing the steps in the below "Building Docker image" heading in the "Running the examples on a K8s cluster" section, but skipping the Minikube specifics(!)), you can run the compose file using `docker-compose up -d` (just like you are used to). The same goes for shutting it down: `docker-compose down`. So nothing new! 


But! There is a way to run this compose file directly in your Kubernetes cluster using a tool called `kompose`! To install `kompose`, you can follow [the instructions on their website](http://kompose.io/). Kompose created Kubernetes resources based upon your `docker-compose.yml` entries! It may not be suitable for production use, but it is a fun and quick way to deploy resources to your Kubernetes cluster! To start, you simply use the command (in the same folder as a `docker-compose.yml` file):
```
kompose up
```

When it's running, it will print all the resources it creates. Now you can play around with it! Try some of the commands from the slides! You also may want to change the service type so you can actually see the frontend in your browser? To do this, you can use the `kubectl patch` command. Change the service to `NodePort` by using:
```
kubectl patch svc bookservice -p '{"spec": {"type": "NodePort"}}'
```

> `kubectl patch` simply "patches" a resource. You give in the part of the definition you want to change, and it will change those parts of the configurations in that resource.


When you are done, simply type:
```
kompose down
```



## Running the examples on a K8s cluster
### Building Docker image
To actually have something to run, I have created a simple application called bookservice. It is what you expect, just a simple application that saves titles, authors and amazon links for books. I added a frontend you can run in your browser just to have something flashy :) 


*If you are using Minikube:* First make sure your Kubernetes image registry is pointing to the same local registry as your Docker install. To do this, type the command `eval $(minikube docker-env)` in your command line.


Now you are ready to proceed. Do the following steps: 
* Navigate to the `bookservice`-folder
* Run the command `docker build -t themkat/k8sintro-bookservice:1.0`. The reason for the name and tag is because we use that in the deployment configuration. 


Now you are ready to run something!


### Deploying to your cluster
Now that everything is built, we can start deploying! 
> *Deployments:* Remember that our `Deployment` contains information on WHAT we are putting into our cluster. This includes the number of replicas ("pod instances"), as well as some selector logic. In an older version, both a `Deployment` and a `ReplicaSet` (container information with the number of replicas set) was seperate resources, so the selectors in the `Deployment` configurations is probably a relic from that time (i.e, created a `ReplicaSet` with a label, then use a selector in our `Deployment` to bind to that `ReplicaSet`). Fortunatly we only have to create `Deployments` these days :) 


Let's start by deploying our database (MongoDB). The `Deployment` resource is described in the file `yaml/database-deployment.yml`, so before we continue you should familiarize yourself with what happens in that file. If you have forgotten about something, you can always refer to [the API docs for Kubernetes resources](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/#deployment-v1-apps). When you feel like you have an okay understanding of the file (it is okay to only understand parts for now!), then we can actually do the deployment! Type the following:
```
kubectl apply -f yaml/database-deployment.yml
```

You can use `kubectl get all` to see the deployment and pods being created, or just `kubectl get pods` to get the pod information. The MongoDB image is quite large, so it may take some time to pull. To get continous updates on what happens, you could try `kubectl get pods -w`. This will start a watcher process, and you will see new pods being created, continuously. 


Now that we have our deployment up and running, let's expose it so other pods in our cluster can communicate with it! This operation is called creating a service. A service configuration can in this case be very minimal; we just have to match the label in our deployment and specify the ports used. This happens in the file `yaml/database-service.yml`, so take a quick look at that file. Just like for `Deployments`, you can always consult the [the API docs for Kubernetes resources](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/#service-v1-core). Type the following to create the service:
```
kubectl apply -f yaml/database-service.yml
```


Now your database service is created, we can deploy the `bookservice` so we have something that talks to it. The files themselves aren't that different from the database deployment. The main difference is the names, labels, and image. So to deploy and expose the `bookservice`, you can do the following:
```
kubectl apply -f yaml/bookservice-deployment.yml
kubectl apply -f yaml/bookservice-service.yml
```


Now everything is up and running! To find the URL for accessing the frontend, you can use the command `minikube service list` if you are using Minikube. To open the service directly in your browser you can use `minikube service bookservice-service`. Now you can play around with it! Scale it, describe it, or do something completely different! See the commands in the slides for some examples.

> *Updating:* So you have updated something in a YAML file? How do you update it in your cluster? You don't have to delete anything! Just run the same `kubectl apply` commands as when you created them! 


### Cleaning
You might want to delete the deployments and services we created? To do that, you can type the following commands:
```
kubectl delete service database
kubectl delete deployment database-deployment
kubectl delete service bookservice-service
kubectl delete deployment bookservice-deployment
```

	
	
## Exercises
Don't know what to do? Try some of the following examples!

### Separating the frontend and backend
Usually, we don't want to bundle both the frontend and backend in the same Docker image. This is done for simplicity here. Some of you may be thinking; "Kubernetes services has DNS entries, why not just use those to communicate with the backend?". This assumption turns out to not work when we have a pure JavaScript application. JavaScript applications run in your browser after all, not in the cluster (they are only served to your browser from the cluster)! There are several ways to solve this. The simplest is to just create a new server that only serves the webpage and has a proxy to the backend server (either a Spring Boot application or something else). Since the server is running inside your cluster, then the proxy would simply forward the requests you do on the webpage. If you are using Spring Boot, then creating a proxy in the frontend server is easy. Either set up a `RESTTemplate` proxy yourself, or use a [library like Zuul Proxy](https://www.baeldung.com/spring-rest-with-zuul-proxy) (there is no `RESTTemplate`, only Zuul... </EndOfHorribleJoke>).


Other ways include experimenting with Ingresses. Both of these options are used in production environments by companies all over the globe. 


Maybe you also have another solution on your mind? :) Feel free to share it!



### Creating an Ingress to get an external IP
> *MiniKube and Docker Kubernetes:* You will in your local setup not get an external IP. This is because Ingress still needs some external program to do this (Google Cloud uses a loadbalancer for this).

Ingresses are another way of exposing your service outside your cluster. Like we mentioned in the slides, it can be thought of as a smart-router (i.e, we set some paths that should route to our services). Take a look at `yaml/bookservice-ingress.yml` to see how an ingress can be defined. When you are ready (and after deploying all the above!), apply the yaml file:
```
kubectl apply -f yaml/bookservice-ingress.yml
```

Now all you have to do is wait a short time. This should not take long with Minikube, but may take more time if you use a cloud platform. To get the ip to connect to, use the command `minikube ip`. Now you can navigate to that IP, and can use the routes you described in your ingress! Pretty nifty, right?

> *Ingress routes*: You can route one Ingress resource to many different services. Example: Let's say we have a service products and a service customerdata. Then we can route these services to their own path! Maybe you want products to be on the /products path of your url, and customerdata to be on the /customers path? Then you can do it with Inrgess! If you want one IP for each service, you can use a `LoadBalancer` instead. 


### Add simple metrics with Prometheus and Grafana (Heapster)
If you use Minikube, you can get metrics very easy! Just run `minikube addons enable heapster`, and Minikube will deploy Prometheus and Grafana to your cluster. From there on out, Prometheus will collect metrics that you can view from Grafana. To look at the metrics, you can use `minikube service list` and find the URL for Grafana. Now try to do some requests and see how CPU usage and other metrics change!


Want to read more? For logging or other features, take a look at the "Monitoring and Metrics" chapter in the [Kubernetes for Developers book](https://www.amazon.com/gp/product/B07931YQK3). 

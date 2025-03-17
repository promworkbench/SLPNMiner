We re-implement this Java plugin in Python with a better open-source non-commercial optimizer, which provides better results than this implementation.  If possible, use the following implementation:
https://github.com/brucelit/slpn-miner/tree/main

# SLPNMiner

SLPNMiner is a ProM package for the discovery of Stochastic Labelled Petri net, which provides plugin-ins for stochastic process discovery. The input are an event log and a Petri net model, and the output is a stochastic labelled petri net. The two current implemented plugins adopt the techniques introduced in the following to assist weight estimation. 

* Entropic relevance:
[Hanan Alkhammash, Artem Polyvyanyy, Alistair Moffat, Luciano García-Bañuelos: Entropic relevance: A mechanism for measuring stochastic process models discovered from event data. Inf. Syst. 107: 101922 (2022)](https://www.sciencedirect.com/science/article/pii/S0306437921001277)

* Unit Earth Mover Stochastic Conformance:
[Sander J. J. Leemans, Wil M. P. van der Aalst, Tobias Brockhoff, Artem Polyvyanyy: Stochastic process mining: Earth movers' stochastic conformance. Inf. Syst. 102: 101724 (2021)](https://www.sciencedirect.com/science/article/pii/S0306437921001277)

## Installation
*  If you have not yet installed or run ProM6 before, follow the installation tutorial: https://promtools.org/prom-6-getting-started/installation/

* Although the majority of ProM developers use jdk 8, I have to use jdk 17 for this project, so that some third-party libraries (requires jdk 11+) can run. Therefore, **jdk 8 is not going to work** for this project*. The following VM argument should be added to make sure ProM GUI can launch with jdk 17: 
    ```python 
    -Djava.system.class.loader=org.processmining.framework.util.ProMClassLoader     
    ```
   <img src="https://github.com/promworkbench/SLPNMiner/blob/main/img/VM_parameter.jpg" style="width:600px; height:250px"/>

## Usage
*  After starting ProM plugin, import the event log and a Petri net model to the GUI.

   <img src="https://github.com/promworkbench/SLPNMiner/blob/main/img/step1.jpg" style="width:600px; height:220px"/>

*  Then, select the plugin-in *Discover SLPN with uEMSC* or *Discover SLPN with Enropic Relevance*.

   <img src="https://github.com/promworkbench/SLPNMiner/blob/main/img/step2.jpg" style="width:600px; height:220px"/>

* The output is a SLPN, which shows the probability value for each transition.
   <img src="https://github.com/promworkbench/SLPNMiner/blob/main/img/step3.jpg" style="width:470px; height:250px"/>

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
L-GPL (https://www.gnu.org/licenses/lgpl-3.0.en.html)

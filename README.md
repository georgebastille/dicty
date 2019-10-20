# Self Organisation in Dictyostelium Discoideum

This project uses a two stage Cellular Automata/Differential Equation model
to simulate the behaviour of Dictyostelium Discoideum (Dd) in the early stages of
aggregation. The model is used to explain the formation of spiral patterns, the
aggregation streams and the increase in speed observed when Dd amoeba travel in
groups. It is then used to show that auto-cycling amoeba may not necessary to
trigger aggregation, and presents an alternative method.

See the [report](dicty_report.pdf) for details.


To run the aggregation simlation:
```bash
cd ./code/final
javac DictyPDEGUI.java
java DictyPDEGUI
```

To run the race simulation:
```bash
cd ./code/final
javac DictyGUI.java
java DictyGUI
```

See also: [Self assembly of a model multicellular organism resembling the Dictyostelium slime molds](https://arxiv.org/abs/0705.0227)

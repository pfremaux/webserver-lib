package webserver.example;


import webserver.annotations.JsonField;
import webserver.validator.ValidationTrait;
import webserver.handlers.web.ErrorReport;

import java.util.Optional;

public class Body  implements ValidationTrait {
/*
TODO PFR En raison de l approche KISS pour ce projet, on va peut etre eviter une anotation @Validate sur chaque endpoint et simplement utiliser une interface ValidationTrait
 qui indiquera que l'on veut valider l'object en input
 En respectant KISS on impose a n'avoir qu'un seul scenario possible pour valider l'input.
 Par ailleurs on peut faire des controles plus specifiques si 2 fields sont dependants en terme de logique. (la ou imposer une validation field par field les isolerait)

  Si on avait cree une annotation @Validate au niveau du endpoint il aurait fallu referencer une class + method a executer. Cela pourrait demander plus de temps a concevoir.1
 * */
	@JsonField
    private final String toto;

    public Body(String toto) {
        this.toto = toto;
    }

    @Override
    public Optional<ErrorReport> validate() {
        if (this.toto.toLowerCase().startsWith("toto")) {
            return Optional.empty();
        }

        return Optional.of(ExampleError.BAD_TOTO);
    }

    public String getToto() {
        return toto;
    }

    @Override
    public String toString() {
        return "Body{" +
                "toto='" + toto + '\'' +
                '}';
    }
}

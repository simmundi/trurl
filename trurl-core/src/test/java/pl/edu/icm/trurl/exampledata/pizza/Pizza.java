package pl.edu.icm.trurl.exampledata.pizza;

import pl.edu.icm.trurl.ecs.annotation.CollectionType;
import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;

@WithMapper
public class Pizza {
    @MappedCollection(margin = 2)
    List<Olive> olives = new ArrayList<>();
    @MappedCollection(minReservation = 10, collectionType = CollectionType.ARRAY_LIST)
    List<Topping> toppings = new ArrayList<>();

    public List<Olive> getOlives() {
        return olives;
    }

    public List<Topping> getToppings() {
        return toppings;
    }
}

package ru.javawebinar.topjava.repository.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryMealRepositoryImpl implements MealRepository {
    private Map<Integer, Meal> repository = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger(0);
    private static final Logger log = LoggerFactory.getLogger(InMemoryMealRepositoryImpl.class);

    {
        MealsUtil.MEALS.forEach(this::save);
    }

    @Override
    public Meal save(Meal meal) {
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            repository.put(meal.getId(), meal);
            return meal;
        }
        // treat case: update, but absent in storage
        return repository.computeIfPresent(meal.getId(), (id, oldMeal) -> meal);
    }

    @Override
    public boolean delete(int id) {
        return repository.remove(id) != null;
    }

    @Override
    public Meal get(int id) {
        return repository.get(id);
    }

    @Override
    public Meal get(int id, int userId) {
        Meal meal = repository.get(id);
        if (meal != null && meal.getUserId() != userId) {
            return null;
        }
        return meal;
    }

    @Override
    public Collection<Meal> getAll() {
        return repository.values();
    }

    @Override
    public List<Meal> getAll(Integer userId) {
        // 3.3: список еды возвращать
        // отсортированный в обратном порядке по датам.
        return repository.entrySet().stream()
                .filter((pair) -> pair.getValue().getUserId().equals(userId))
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparing(Meal::getDate).reversed()) // This is cool :-]
                .collect(Collectors.toList());
    }

}


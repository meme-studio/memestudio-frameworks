package dev.memestudio.framework.common.support;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function5;
import io.vavr.Predicates;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

import static java.util.stream.Collectors.*;

/**
 * @author meme
 * @since 2020/7/3
 */
@UtilityClass
public class CollectorsEx {

    private final Object ROOT_ID = new Object();

    /**
     * 父节点parentId需为null
     * <p/>
     * Example：
     * <pre>
     * mapper.selectByExample(new TakeoutMenuTagCategoryExample())
     *       .stream()
     *       .map(TagCategory::from)
     *       .collect(mergingTrees(TagCategory::getId, TagCategory::getParentId, TagCategory::setChildren));
     * </pre>
     */
    public <T, ID> Collector<T, ?, List<T>> toTrees(Function<T, ID> idMapper,
                                                           Function<T, ID> parentMapper,
                                                           BiConsumer<T, List<T>> childrenSetter) {
        return toTrees(idMapper, parentMapper, childrenSetter, Function2.constant(0)::apply);
    }

    /**
     * 父节点parentId需为null
     * <p/>
     * Example：
     * <pre>
     * mapper.selectByExample(new TakeoutMenuTagCategoryExample())
     *       .stream()
     *       .map(TagCategory::from)
     *       .collect(mergingTrees(TagCategory::getId, TagCategory::getParentId, TagCategory::setChildren, Comparator.comparing(TagCategory::getOrdered)));
     * </pre>
     */
    public <T, ID> Collector<T, ?, List<T>> toTrees(Function<T, ID> idMapper,
                                                           Function<T, ID> parentMapper,
                                                           BiConsumer<T, List<T>> childrenSetter,
                                                           Comparator<T> orderComparator) {
        return toTrees(
                idMapper,
                parentMapper,
                childrenSetter,
                orderComparator,
                Function1.constant(Collections.emptyList()),
                Function1.constant(true)::apply);
    }

    /**
     * 父节点parentId需为null
     * <p/>
     * Example：
     * <pre>
     * mapper.selectByExample(new TakeoutMenuTagCategoryExample())
     *       .stream()
     *       .map(TagCategory::from)
     *       .collect(mergingTrees(
     *              TagCategory::getId,
     *              TagCategory::getParentId,
     *              TagCategory::setChildren,
     *              Comparator.comparing(TagCategory::getOrdered),
     *              TagCategory::getChildren,
     *              category -> category.getName().contains("a")
     *        ));
     * </pre>
     */
    public <T, ID> Collector<T, ?, List<T>> toTrees(Function<T, ID> idMapper,
                                                           Function<T, ID> parentMapper,
                                                           BiConsumer<T, List<T>> childrenSetter,
                                                           Comparator<T> orderComparator,
                                                           Function<T, List<T>> childrenGetter,
                                                           Predicate<T> filter) {
        Objects.requireNonNull(idMapper, "idMapper is null");
        Objects.requireNonNull(parentMapper, "parentMapper is null");
        Objects.requireNonNull(childrenSetter, "childrenSetter is null");
        Objects.requireNonNull(orderComparator, "orderComparator is null");
        Objects.requireNonNull(childrenGetter, "childrenGetter is null");
        Objects.requireNonNull(filter, "filter is null");
        return collectingAndThen(
                groupingBy(parentMapper.andThen(Optional::ofNullable)
                                       .andThen(Function2.<Optional<ID>, Function<ID, Object>, Optional<Object>>of(Optional::map).reversed().apply(Object.class::cast))
                                       .andThen(Function2.<Optional<Object>, Object, Object>of(Optional::orElse).reversed().apply(ROOT_ID))),
                leaves -> leaves.values()
                                .stream()
                                .flatMap(Collection::stream)
                                .peek(node -> childrenSetter.accept(node, Optional.ofNullable(node)
                                                                                  .map(idMapper)
                                                                                  .map(leaves::get)
                                                                                  .orElseGet(Collections::emptyList)))
                                .collect(collectingAndThen(
                                        toList(),
                                        parents -> parents.stream()
                                                          .filter(Function1.of(Objects::isNull)
                                                                           .compose(parentMapper)::apply)
                                                          .collect(collectingAndThen(
                                                                  toList(),
                                                                  Function5.<BiConsumer<T, List<T>>, Function<T, List<T>>, Predicate<T>, Comparator<T>, List<T>, List<T>>of(CollectorsEx::filterAndSortTreeRecursely)
                                                                          .apply(childrenSetter, childrenGetter, filter, orderComparator))))));
    }

    private <T> List<T> filterAndSortTreeRecursely(BiConsumer<T, List<T>> childrenSetter,
                                                          Function<T, List<T>> childrenGetter,
                                                          Predicate<T> filter,
                                                          Comparator<T> orderComparator,
                                                          List<T> parents) {
        return parents.stream()
                      .peek(parent -> Optional.of(parent)
                                              .filter(filter.negate())
                                              .map(childrenGetter)
                                              .ifPresent(
                                                      children -> childrenSetter.accept(
                                                              parent,
                                                              filterAndSortTreeRecursely(
                                                                      childrenSetter,
                                                                      childrenGetter,
                                                                      filter,
                                                                      orderComparator,
                                                                      children))))
                      .filter(filter.or(Predicates.not(childrenGetter.andThen(List::isEmpty)::apply)))
                      .sorted(orderComparator)
                      .collect(toList());
    }

}

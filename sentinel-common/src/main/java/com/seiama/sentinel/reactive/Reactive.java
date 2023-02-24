package com.seiama.sentinel.reactive;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple5;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuple7;
import reactor.util.function.Tuple8;
import reactor.util.function.Tuples;

public interface Reactive {
  static <T1, T2> Mono<Tuple2<T1, T2>> zipSequence(
    final Mono<? extends T1> m1,
    final Mono<? extends T2> m2
  ) {
    return m1.flatMap(t1 -> {
      return m2.map(t2 -> {
        return Tuples.of(t1, t2);
      });
    });
  }

  static <T1, T2, T3> Mono<Tuple3<T1, T2, T3>> zipSequence(
    final Mono<? extends T1> m1,
    final Mono<? extends T2> m2,
    final Mono<? extends T3> m3
  ) {
    return m1.flatMap(t1 -> {
      return m2.flatMap(t2 -> {
        return m3.map(t3 -> {
          return Tuples.of(t1, t2, t3);
        });
      });
    });
  }

  static <T1, T2, T3, T4> Mono<Tuple4<T1, T2, T3, T4>> zipSequence(
    final Mono<? extends T1> m1,
    final Mono<? extends T2> m2,
    final Mono<? extends T3> m3,
    final Mono<? extends T4> m4
  ) {
    return m1.flatMap(t1 -> {
      return m2.flatMap(t2 -> {
        return m3.flatMap(t3 -> {
          return m4.map(t4 -> {
            return Tuples.of(t1, t2, t3, t4);
          });
        });
      });
    });
  }

  static <T1, T2, T3, T4, T5> Mono<Tuple5<T1, T2, T3, T4, T5>> zipSequence(
    final Mono<? extends T1> m1,
    final Mono<? extends T2> m2,
    final Mono<? extends T3> m3,
    final Mono<? extends T4> m4,
    final Mono<? extends T5> m5
  ) {
    return m1.flatMap(t1 -> {
      return m2.flatMap(t2 -> {
        return m3.flatMap(t3 -> {
          return m4.flatMap(t4 -> {
            return m5.map(t5 -> {
              return Tuples.of(t1, t2, t3, t4, t5);
            });
          });
        });
      });
    });
  }

  static <T1, T2, T3, T4, T5, T6> Mono<Tuple6<T1, T2, T3, T4, T5, T6>> zipSequence(
    final Mono<? extends T1> m1,
    final Mono<? extends T2> m2,
    final Mono<? extends T3> m3,
    final Mono<? extends T4> m4,
    final Mono<? extends T5> m5,
    final Mono<? extends T6> m6
  ) {
    return m1.flatMap(t1 -> {
      return m2.flatMap(t2 -> {
        return m3.flatMap(t3 -> {
          return m4.flatMap(t4 -> {
            return m5.flatMap(t5 -> {
              return m6.map(t6 -> {
                return Tuples.of(t1, t2, t3, t4, t5, t6);
              });
            });
          });
        });
      });
    });
  }

  static <T1, T2, T3, T4, T5, T6, T7> Mono<Tuple7<T1, T2, T3, T4, T5, T6, T7>> zipSequence(
    final Mono<? extends T1> m1,
    final Mono<? extends T2> m2,
    final Mono<? extends T3> m3,
    final Mono<? extends T4> m4,
    final Mono<? extends T5> m5,
    final Mono<? extends T6> m6,
    final Mono<? extends T7> m7
  ) {
    return m1.flatMap(t1 -> {
      return m2.flatMap(t2 -> {
        return m3.flatMap(t3 -> {
          return m4.flatMap(t4 -> {
            return m5.flatMap(t5 -> {
              return m6.flatMap(t6 -> {
                return m7.map(t7 -> {
                  return Tuples.of(t1, t2, t3, t4, t5, t6, t7);
                });
              });
            });
          });
        });
      });
    });
  }

  static <T1, T2, T3, T4, T5, T6, T7, T8> Mono<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> zipSequence(
    final Mono<? extends T1> m1,
    final Mono<? extends T2> m2,
    final Mono<? extends T3> m3,
    final Mono<? extends T4> m4,
    final Mono<? extends T5> m5,
    final Mono<? extends T6> m6,
    final Mono<? extends T7> m7,
    final Mono<? extends T8> m8
  ) {
    return m1.flatMap(t1 -> {
      return m2.flatMap(t2 -> {
        return m3.flatMap(t3 -> {
          return m4.flatMap(t4 -> {
            return m5.flatMap(t5 -> {
              return m6.flatMap(t6 -> {
                return m7.flatMap(t7 -> {
                  return m8.map(t8 -> {
                    return Tuples.of(t1, t2, t3, t4, t5, t6, t7, t8);
                  });
                });
              });
            });
          });
        });
      });
    });
  }
}

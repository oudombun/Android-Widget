/**
 * Created by monyoudom on  26/07/2022.
 */

package com.oudombun.widgettesting.event

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

object RxBus {

   private val publisher = PublishSubject.create<Any>()

   fun publish(event: Any) {
      publisher.onNext(event)
   }

   // Listen should return an Observable and not the publisher
   // Using ofType we filter only events that match that class type
   fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}
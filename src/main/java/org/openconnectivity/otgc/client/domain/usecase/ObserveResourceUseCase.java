/*
 * Copyright 2018 DEKRA Testing and Certification, S.A.U. All Rights Reserved.
 *
 * *****************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openconnectivity.otgc.client.domain.usecase;

import io.reactivex.Observable;
import org.iotivity.base.OcResource;
import org.openconnectivity.otgc.client.data.repository.ResourceRepository;
import org.openconnectivity.otgc.client.domain.model.SerializableResource;
import org.openconnectivity.otgc.common.data.repository.IotivityRepository;

import javax.inject.Inject;

public class ObserveResourceUseCase {

    private final IotivityRepository iotivityRepository;
    private final ResourceRepository resourceRepository;

    @Inject
    public ObserveResourceUseCase(IotivityRepository iotivityRepository,
                                  ResourceRepository resourceRepository) {
        this.iotivityRepository = iotivityRepository;
        this.resourceRepository = resourceRepository;
    }

    public Observable<SerializableResource> execute (SerializableResource resource) {
        return iotivityRepository.getCoapsHost(resource.getHosts())
                .flatMap(host -> iotivityRepository.constructResource(host, resource.getUri(), resource.getResourceTypes(), resource.getResourceInterfaces()))
                .flatMapObservable(ocResource -> resourceRepository.observeResource(ocResource, resource));
    }


}
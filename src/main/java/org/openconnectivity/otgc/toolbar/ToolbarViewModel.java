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

package org.openconnectivity.otgc.toolbar;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import io.reactivex.disposables.CompositeDisposable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import org.apache.log4j.Logger;
import org.iotivity.base.OcSecureResource;
import org.iotivity.base.OxmType;
import org.openconnectivity.otgc.common.constant.NotificationConstant;
import org.openconnectivity.otgc.common.domain.usecase.GetDeviceInfoUseCase;
import org.openconnectivity.otgc.common.domain.usecase.GetDeviceNameUseCase;
import org.openconnectivity.otgc.common.domain.usecase.GetDeviceRoleUseCase;
import org.openconnectivity.otgc.common.domain.usecase.SetDeviceNameUseCase;
import org.openconnectivity.otgc.devicelist.domain.model.Device;
import org.openconnectivity.otgc.devicelist.domain.model.DeviceType;
import org.openconnectivity.otgc.common.rx.SchedulersFacade;
import org.openconnectivity.otgc.common.viewmodel.Response;
import org.openconnectivity.otgc.scopes.DeviceListToolbarDetailScope;
import org.openconnectivity.otgc.toolbar.usecase.*;

import javax.inject.Inject;
import java.util.List;

@ScopeProvider(scopes = DeviceListToolbarDetailScope.class)
public class ToolbarViewModel implements ViewModel {

    private final Logger LOG = Logger.getLogger(ToolbarViewModel.class);

    public ObjectProperty<Device> deviceProperty;
    private IntegerProperty positionDevice;
    public IntegerProperty positionDeviceProperty() {
        return this.positionDevice;
    }

    @InjectScope
    private DeviceListToolbarDetailScope deviceListToolbarDetailScope;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private final SchedulersFacade schedulersFacade;
    private final GetOTMethodsUseCase getOTMethodsUseCase;
    private final SetOTMethodUseCase setOTMethodUseCase;
    private final OnboardUseCase onboardUseCase;
    private final GetDeviceInfoUseCase getDeviceInfoUseCase;
    private final GetDeviceNameUseCase getDeviceNameUseCase;
    private final SetDeviceNameUseCase setDeviceNameUseCase;
    private final SetRfotmModeUseCase setRfotmModeUseCase;
    private final SetRfnopModeUseCase setRfnopModeUseCase;
    private final GetDeviceIdUseCase getDeviceIdUseCase;
    private final RefreshSecResourceUseCase refreshSecResourceUseCase;
    private final OffboardDeviceUseCase offboardDeviceUseCase;
    private final RefreshUnsecResourceUseCase refreshUnsecResourceUseCase;
    private final GetDeviceRoleUseCase getDeviceRoleUseCase;

    // Observable responses
    private final ObjectProperty<Response<Device>> otmResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> refreshResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> offboardResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> rfotmResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Void>> rfnopResponse = new SimpleObjectProperty<>();
    private final ObjectProperty<Response<Device>> refreshUnownedResponse = new SimpleObjectProperty<>();

    private SelectOxMListener oxmListener;

    public void initialize() {
        deviceProperty = deviceListToolbarDetailScope.selectedDeviceProperty();
        positionDevice = deviceListToolbarDetailScope.positionSelectedDeviceProperty();
    }

    @Inject
    public ToolbarViewModel(SchedulersFacade schedulersFacade,
                    GetOTMethodsUseCase getOTMethodsUseCase,
                    SetOTMethodUseCase setOTMethodUseCase,
                    OnboardUseCase onboardUseCase,
                    GetDeviceInfoUseCase getDeviceInfoUseCase,
                    GetDeviceNameUseCase getDeviceNameUseCase,
                    SetDeviceNameUseCase setDeviceNameUseCase,
                    SetRfotmModeUseCase setRfotmModeUseCase,
                    SetRfnopModeUseCase setRfnopModeUseCase,
                    GetDeviceIdUseCase getDeviceIdUseCase,
                    RefreshSecResourceUseCase refreshSecResourceUseCase,
                    OffboardDeviceUseCase offboardDeviceUseCase,
                    RefreshUnsecResourceUseCase refreshUnsecResourceUseCase,
                    GetDeviceRoleUseCase getDeviceRoleUseCase) {
        this.schedulersFacade = schedulersFacade;
        this.getOTMethodsUseCase = getOTMethodsUseCase;
        this.setOTMethodUseCase = setOTMethodUseCase;
        this.onboardUseCase = onboardUseCase;
        this.getDeviceInfoUseCase = getDeviceInfoUseCase;
        this.getDeviceNameUseCase = getDeviceNameUseCase;
        this.setDeviceNameUseCase = setDeviceNameUseCase;
        this.setRfotmModeUseCase = setRfotmModeUseCase;
        this.setRfnopModeUseCase = setRfnopModeUseCase;
        this.getDeviceIdUseCase = getDeviceIdUseCase;
        this.refreshSecResourceUseCase = refreshSecResourceUseCase;
        this.offboardDeviceUseCase = offboardDeviceUseCase;
        this.refreshUnsecResourceUseCase = refreshUnsecResourceUseCase;
        this.getDeviceRoleUseCase = getDeviceRoleUseCase;
    }

    public ObservableBooleanValue onboardButtonDisabled() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() == null
                        || deviceProperty.get().getDeviceType() != DeviceType.UNOWNED, deviceProperty);
    }

    public ObservableBooleanValue offboardButtonDisabled() {
        return Bindings.createBooleanBinding(() -> deviceProperty.get() == null
                || deviceProperty.get().getDeviceType() != DeviceType.OWNED_BY_SELF, deviceProperty);
    }

    public void setOxmListener(SelectOxMListener listener) {
        this.oxmListener = listener;
    }

    public ObjectProperty<Response<Device>> otmResponseProperty() {
        return otmResponse;
    }

    public ObjectProperty<Response<Device>> refreshResponseProperty() {
        return refreshResponse;
    }

    public ObjectProperty<Response<Device>> offboardResponseProperty() {
        return offboardResponse;
    }

    public ObjectProperty<Response<Void>> rfotmResponseProperty() {
        return rfotmResponse;
    }

    public ObjectProperty<Response<Void>> rfnopResponseProperty() {
        return rfnopResponse;
    }

    public ObjectProperty<Response<Device>> refreshUnownedResponseProperty() {
        return refreshUnownedResponse;
    }

    public void doOwnershipTransfer(OcSecureResource ocSecureResource) {
        disposables.add(getOTMethodsUseCase.execute(ocSecureResource)
                .map(oxms -> {
                    if (oxms.size() > 1) {
                        return oxmListener.onGetOxM(oxms);
                    } else {
                        return oxms.get(0);
                    }
                }).filter(oxm -> oxm != null)
                .flatMapCompletable(oxm -> setOTMethodUseCase.execute(ocSecureResource, oxm))
                .andThen(onboardUseCase.execute(ocSecureResource)
                        .map(device -> {
                            device.setDeviceInfo(getDeviceInfoUseCase.execute(device.getDeviceId()).blockingGet());
                            return device;
                        }))
                        .map(device -> {
                            device.setRole(getDeviceRoleUseCase.execute(device.getDeviceId()).blockingGet());
                            return device;
                        })
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> otmResponse.setValue(Response.loading()))
                .subscribe(
                        ownedDevice -> otmResponse.setValue(Response.success(ownedDevice)),
                        throwable -> otmResponse.setValue(Response.error(throwable))
                ));
    }

    public void updateDeviceToOwned(String deviceId) {
        disposables.add(refreshSecResourceUseCase.execute(deviceId)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> refreshResponse.setValue(Response.loading()))
                .subscribe(
                        ownedDevice -> refreshResponse.setValue(Response.success(ownedDevice)),
                        throwable -> refreshResponse.setValue(Response.error(throwable))
                ));
    }

    public void onScanPressed() {
        deviceListToolbarDetailScope.publish(NotificationConstant.SCAN_DEVICES);
    }

    public void updateItem(int positionToUpdate, Device deviceToUpdate) {
        deviceListToolbarDetailScope.publish(NotificationConstant.UPDATE_DEVICE, positionToUpdate, deviceToUpdate);
    }

    public void offboard(OcSecureResource deviceToOffboard) {
        disposables.add(offboardDeviceUseCase.execute(deviceToOffboard)
            .map(device -> {
                device.setDeviceInfo(getDeviceInfoUseCase.execute(device.getDeviceId()).blockingGet());
                return device;
            })
            .subscribeOn(schedulersFacade.io())
            .observeOn(schedulersFacade.ui())
            .doOnSubscribe(__ -> offboardResponse.setValue(Response.loading()))
            .subscribe(
                    unownedDevice -> offboardResponse.setValue(Response.success(unownedDevice)),
                    throwable -> offboardResponse.setValue(Response.error(throwable))
            )
        );
    }

    public void updateDeviceToUnowned(String deviceId) {
        disposables.add(refreshUnsecResourceUseCase.execute(deviceId)
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> refreshUnownedResponse.setValue(Response.loading()))
                .subscribe(
                        ownedDevice -> refreshUnownedResponse.setValue(Response.success(ownedDevice)),
                        throwable -> refreshUnownedResponse.setValue(Response.error(throwable))
                ));
    }

    public void setDeviceName(String deviceId, String deviceName) {
        disposables.add(setDeviceNameUseCase.execute(deviceId, deviceName)
            .andThen(getDeviceNameUseCase.execute(deviceId))
            .subscribeOn(schedulersFacade.io())
            .observeOn(schedulersFacade.ui())
            .subscribe(
                    name -> {},
                    throwable -> {}
            ));
    }

    public void setRfotmMode() {
        disposables.add(setRfotmModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> rfotmResponse.setValue(Response.loading()))
                .subscribe(
                        () -> rfotmResponse.setValue(Response.success(null)),
                        throwable -> rfotmResponse.setValue(Response.error(throwable))
                ));
    }

    public void setRfnopMode() {
        disposables.add(setRfnopModeUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .doOnSubscribe(__ -> rfnopResponse.setValue(Response.loading()))
                .subscribe(
                        () -> rfnopResponse.setValue(Response.success(null)),
                        throwable -> rfnopResponse.setValue(Response.error(throwable))
                )
        );
    }

    public void retrieveDeviceId() {
        disposables.add(getDeviceIdUseCase.execute()
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        id -> LOG.debug("ID: " + id),
                        throwable -> {}
                ));
    }

    public interface SelectOxMListener {
        OxmType onGetOxM(List<OxmType> supportedOxm);
    }
}

/*
 * Copyright 2017-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.koin.androidx.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import org.koin.androidx.viewmodel.ViewModelParameter
import org.koin.androidx.viewmodel.ext.android.getViewModelFactory
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.androidx.viewmodel.scope.BundleDefinition
import org.koin.androidx.viewmodel.scope.emptyState
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import kotlin.reflect.KClass

/**
 * Resolve ViewModel instance
 *
 * @param qualifier
 * @param parameters
 *
 * @author Arnaud Giuliani
 */

@OptIn(KoinInternalApi::class)
@Composable
inline fun <reified T : ViewModel> getViewModel(
    qualifier: Qualifier? = null,
    owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    scope: Scope = GlobalContext.get().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null
): T {
    return remember(qualifier, parameters) {
        val vmClazz = T::class
        val currentBundle = (owner as? NavBackStackEntry)?.arguments
        val factory = getViewModelFactory(
            owner, vmClazz, qualifier, parameters, scope = scope, state = currentBundle?.let { {it} }
        )
        val viewModelProvider = ViewModelProvider(owner, factory)
        return@remember resolveViewModelFromProvider(qualifier, viewModelProvider, vmClazz)
    }
}

@OptIn(KoinInternalApi::class)
@Composable
inline fun <reified T : ViewModel> koinViewModel(
    qualifier: Qualifier? = null,
    owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    scope: Scope = GlobalContext.get().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null
): T {
    return getViewModel(qualifier, owner, scope, parameters)
}

@PublishedApi
internal inline fun <reified T : ViewModel> resolveViewModelFromProvider(
    qualifier: Qualifier?,
    viewModelProvider: ViewModelProvider,
    vmClazz: KClass<T>
): T {
    val viewModel: T = if (qualifier == null) {
        viewModelProvider[vmClazz.java]
    } else {
        viewModelProvider[qualifier.value, vmClazz.java]
    }
    return viewModel
}

@OptIn(KoinInternalApi::class)
@Deprecated("ViewModelLazy API is not supported by Jetpack Compose 1.1+. Please use koinViewModel(qualifier,owner,scope,parameters)",level = DeprecationLevel.ERROR)
@Composable
inline fun <reified T : ViewModel> viewModel(
    qualifier: Qualifier? = null,
    owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    scope: Scope = GlobalContext.get().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null
): ViewModelLazy<T> = error("ViewModelLazy API is not supported by Jetpack Compose 1.1+")

/**
 * Resolve ViewModel instance
 *
 * @param qualifier
 * @param parameters
 *
 * @author Arnaud Giuliani
 */
@OptIn(KoinInternalApi::class)
@Composable
inline fun <reified T : ViewModel> getStateViewModel(
    qualifier: Qualifier? = null,
    owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    scope: Scope = GlobalContext.get().scopeRegistry.rootScope,
    noinline state: BundleDefinition,
    noinline parameters: ParametersDefinition? = null,
): T {
    val stateOwner = LocalSavedStateRegistryOwner.current
    return remember(qualifier, parameters) {
        val vmClazz = T::class
        val factory = scope.getViewModelFactory(
            ViewModelParameter(
                vmClazz, qualifier, state, parameters, owner, stateOwner
            )
        )
        val viewModelProvider = ViewModelProvider(owner, factory)
        resolveViewModelFromProvider(qualifier, viewModelProvider, vmClazz)
    }
}
import Foundation
import SwiftUI
import Shared

@MainActor
@Observable
final class ChatViewModelBridge {
    let viewModel: MainViewModel
    private(set) var state: ChatUiState
    private(set) var pendingError: String? = nil

    init() {
        let deps = IOSDependencies()
        let vm = deps.chatViewModel
        self.viewModel = vm
        self.state = vm.state.value
        observeState()
        observeEffects()
    }

    private func observeState() {
        Task { @MainActor [weak self] in
            guard let self else { return }
            for await s in self.viewModel.state {
                self.state = s
            }
        }
    }

    private func observeEffects() {
        Task { @MainActor [weak self] in
            guard let self else { return }
            for await effect in self.viewModel.effects {
                if let showError = effect as? EffectMainShowError {
                    self.pendingError = showError.message
                }
            }
        }
    }

    func dispatch(_ intent: Intent) {
        viewModel.dispatch(intent: intent)
    }

    func clearError() {
        pendingError = nil
        dispatch(IntentMainDismissError.shared)
    }
}

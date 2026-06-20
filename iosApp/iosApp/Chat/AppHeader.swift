import SwiftUI
import Shared

struct AppHeader: View {
    let state: ChatUiState
    let onMenuTapped: () -> Void
    let onLogoTapped: () -> Void
    let onNewChatTapped: () -> Void

    var body: some View {
        HStack {
            Button(action: onMenuTapped) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundStyle(.white)
                    .frame(width: 44, height: 44)
            }
            Spacer()
            LogoButton(state: state, onTap: onLogoTapped)
            Spacer()
            Button(action: onNewChatTapped) {
                Image(systemName: "plus")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundStyle(.white)
                    .frame(width: 44, height: 44)
            }
            .disabled(!isNewChatEnabled)
            .opacity(isNewChatEnabled ? 1 : 0.4)
        }
        .padding(.horizontal, 8)
        .frame(height: 56)
    }

    private var isNewChatEnabled: Bool {
        !state.turns.isEmpty || !state.input.isEmpty
    }
}

private struct LogoButton: View {
    let state: ChatUiState
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack {
                if let pct = state.downloadProgressPct {
                    Circle()
                        .trim(from: 0, to: CGFloat(pct.intValue) / 100.0)
                        .stroke(Color.white.opacity(0.9), lineWidth: 2)
                        .rotationEffect(.degrees(-90))
                        .frame(width: 44, height: 44)
                } else if state.isInitializing {
                    ProgressView()
                        .progressViewStyle(.circular)
                        .tint(.white)
                        .frame(width: 44, height: 44)
                }
                Image("WolfLogo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 40, height: 40)
                    .opacity(state.isReady ? 1.0 : 0.4)
            }
            .frame(width: 48, height: 48)
        }
        .disabled(!state.isLogoTappable)
    }
}

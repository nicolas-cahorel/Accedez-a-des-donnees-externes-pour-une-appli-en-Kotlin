package com.aura.data.repository

import android.util.Log
import com.aura.data.model.AccountsResultModel
import com.aura.data.network.AccountClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Repository responsible for handling user account-related data operations.
 * This class interacts with the [AccountClient] to perform user account API requests.
 */
class AccountRepository(private val dataService: AccountClient) {

    /**
     * Fetches user account data based on the provided user ID.
     *
     * @param userId The ID associated with the user account.
     * @return A [Flow] emitting [AccountsResultModel] objects based on the API response.
     */
    fun fetchAccountData(userId: String): Flow<AccountsResultModel> =
        flow<AccountsResultModel> {

            // Retrieve user account information using the provided user ID
            val accountApiResponse = dataService.getUserAccount(userId)

            // Extract the status code and response body from the API response
            val accountApiStatusCode = accountApiResponse.code()
            val accountApiResponseBody = accountApiResponse.body()

            // Determine the AccountsResultModel based on the API response
            val accountsResultModel = when {

                // Case 1: Both response body and status code are not null
                accountApiResponseBody != null && accountApiStatusCode != null -> {
                    AccountsResultModel(
                        accountApiStatusCode,
                        accountApiResponseBody.map { it.toDomainModel() }
                    )
                }

                // Case 2: Response body is null but status code is not null
                accountApiResponseBody == null && accountApiStatusCode != null -> {
                    AccountsResultModel(
                        accountApiStatusCode,
                        accounts = emptyList()
                    )
                }

                // Case 3: Response body is not null but status code is null
                accountApiResponseBody != null && accountApiStatusCode == null -> {
                    AccountsResultModel(
                        accountStatusCode = 1,
                        accounts = accountApiResponseBody.map { it.toDomainModel() }
                    )
                }

                // Case 4: Both response body and status code are null
                accountApiResponseBody == null && accountApiStatusCode == null -> {
                    AccountsResultModel(
                        accountStatusCode = 0,
                        accounts = emptyList()
                    )
                }

                // Fallback case to handle unexpected scenarios
                else -> {
                    AccountsResultModel(
                        accountStatusCode = 2,
                        accounts = emptyList()
                    )
                }
            }

            // Emit the AccountsResultModel to the flow
            emit(accountsResultModel)

        }.catch { error ->
            // Handle any errors that occur during the flow
            Log.e("UserAccountRepository", error.message ?: "No exception message")
        }
}
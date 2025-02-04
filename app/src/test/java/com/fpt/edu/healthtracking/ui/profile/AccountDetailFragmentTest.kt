//import com.fpt.edu.healthtracking.api.Resource
//import com.fpt.edu.healthtracking.data.model.ProfileData
//import com.fpt.edu.healthtracking.data.repository.AccountRepository
//import com.fpt.edu.healthtracking.ui.profile.AccountDetailViewModel
//import io.mockk.*
//import io.mockk.impl.annotations.MockK
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.TestCoroutineDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runBlockingTest
//import kotlinx.coroutines.test.setMain
//import okhttp3.ResponseBody
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.junit.runners.JUnit4
//import retrofit2.Response
//
//@ExperimentalCoroutinesApi
//@RunWith(JUnit4::class)
//class AccountDetailViewModelTest {
//
//    private val testDispatcher = TestCoroutineDispatcher()
//
//    @MockK
//    private lateinit var repository: AccountRepository
//
//    private lateinit var viewModel: AccountDetailViewModel
//
//    @Before
//    fun setup() {
//        MockKAnnotations.init(this)
//        Dispatchers.setMain(testDispatcher)
//        viewModel = AccountDetailViewModel(repository)
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//        testDispatcher.cleanupTestCoroutines()
//        unmockkAll()
//    }
//
//    @Test
//    fun `loadAccountData success should update profileData`() = runBlockingTest {
//        // Given
//        val token = "test-token"
//        val mockProfileData = ProfileData(
//            username = "Test User",
//            email = "test@example.com",
//            phoneNumber = "1234567890",
//            dob = "1990-01-01",
//            gender = true,
//            height = 170,
//            weight = 70
//        )
//        coEvery { repository.getMemberProfile() } returns Resource.Success(mockProfileData)
//
//        // When
//        viewModel.loadAccountData(token)
//
//        // Then
//        coVerify { repository.getMemberProfile() }
//        assert(viewModel.profileData.value == mockProfileData)
//        assert(viewModel.error.value == null)
//    }
//
//    @Test
//    fun `loadAccountData failure should update error`() = runBlockingTest {
//        // Given
//        val token = "test-token"
//        val mockErrorBody = mockk<ResponseBody>()
//        coEvery { repository.getMemberProfile() } returns Resource.Failure(
//            isNetworkError = true,
//            errorCode = 400,
//            errorBody = mockErrorBody
//        )
//
//        // When
//        viewModel.loadAccountData(token)
//
//        // Then
//        coVerify { repository.getMemberProfile() }
//        assert(viewModel.profileData.value == null)
//        assert(viewModel.error.value != null)
//    }
//
//    @Test
//    fun `updateProfileData success should update profileData`() = runBlockingTest {
//        // Given
//        val mockProfileData = ProfileData(
//            username = "Test User",
//            email = "test@example.com",
//            phoneNumber = "1234567890",
//            memberImage = "image_url",
//            dob = "1990-01-01",
//            gender = true,
//            height = 170,
//            weight = 70
//        )
//        coEvery { repository.updateProfile(any()) } returns Response.success(mockProfileData)
//
//        // When
//        viewModel.updateProfileData(mockProfileData)
//
//        // Then
//        coVerify { repository.updateProfile(mockProfileData) }
//        assert(viewModel.profileData.value == mockProfileData)
//        assert(viewModel.error.value == null)
//    }
//
//    @Test
//    fun `updateProfileData failure should update error`() = runBlockingTest {
//        // Given
//        val mockProfileData = ProfileData(
//            username = "Test User",
//            email = "test@example.com",
//            phoneNumber = "1234567890",
//            memberImage = "image_url",
//            dob = "1990-01-01",
//            gender = true,
//            height = 170,
//            weight = 70
//        )
//        val mockErrorBody = mockk<ResponseBody>()
//        coEvery { repository.updateProfile(any()) } returns Resource.Failure(
//            isNetworkError = false,
//            errorCode = 400,
//            errorBody = mockErrorBody
//        )
//
//        // When
//        viewModel.updateProfileData(mockProfileData)
//
//        // Then
//        coVerify { repository.updateProfile(mockProfileData) }
//        assert(viewModel.profileData.value == null)
//        assert(viewModel.error.value != null)
//    }
//    @Test
//    fun `setDob should update profileData with new date`() = runBlockingTest {
//        // Given
//        val mockProfileData = ProfileData(
//            username = "Test User",
//            email = "test@example.com",
//            phoneNumber = "1234567890",
//            memberImage = "image_url",
//            dob = "1990-01-01",
//            gender = true,
//            height = 170,
//            weight = 70
//        )
//        coEvery { repository.getMemberProfile() } returns Resource.Success(mockProfileData)
//        viewModel.loadAccountData("")
//
//        val newDob = "1995-02-15"
//
//        // When
//        viewModel.setDob(newDob)
//
//        // Then
//        assert(viewModel.profileData.value?.dob == newDob)
//    }
//
//    @Test
//    fun `validation of empty profile data should return false`() {
//        // Given
//        val emptyProfile = ProfileData(
//            username = "",
//            email = "",
//            phoneNumber = "",
//            memberImage = "",
//            dob = "",
//            gender = true,
//            height = 0,
//            weight = 0
//        )
//
//        val validator = ProfileValidator()
//
//        // When
//        val isNameValid = validator.validateName(emptyProfile.username)
//        val isEmailValid = validator.validateEmail(emptyProfile.email)
//        val isPhoneValid = validator.validatePhone(emptyProfile.phoneNumber)
//        val dobError = validator.validateDob(emptyProfile.dob)
//        val isHeightValid = validator.validateHeight(emptyProfile.height.toString())
//        val isWeightValid = validator.validateWeight(emptyProfile.weight.toString())
//
//        // Then
//        assert(!isNameValid)
//        assert(!isEmailValid)
//        assert(!isPhoneValid)
//        assert(dobError != null)
//        assert(!isHeightValid)
//        assert(!isWeightValid)
//    }
//
//    @Test
//    fun `validation of valid profile data should return true`() {
//        // Given
//        val validProfile = ProfileData(
//            username = "Test User",
//            email = "test@example.com",
//            phoneNumber = "1234567890",
//            memberImage = "image_url",
//            dob = "1990-01-01",
//            gender = true,
//            height = 170,
//            weight = 70
//        )
//
//        val validator = ProfileValidator()
//
//        // When
//        val isNameValid = validator.validateName(validProfile.username)
//        val isEmailValid = validator.validateEmail(validProfile.email)
//        val isPhoneValid = validator.validatePhone(validProfile.phoneNumber)
//        val dobError = validator.validateDob(validProfile.dob)
//        val isHeightValid = validator.validateHeight(validProfile.height.toString())
//        val isWeightValid = validator.validateWeight(validProfile.weight.toString())
//
//        // Then
//        assert(isNameValid)
//        assert(isEmailValid)
//        assert(isPhoneValid)
//        assert(dobError == null)
//        assert(isHeightValid)
//        assert(isWeightValid)
//    }
//
//}
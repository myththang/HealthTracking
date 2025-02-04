package com.fpt.edu.healthtracking.ui.profile

import com.fpt.edu.healthtracking.util.CustomMarkerView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.api.GoalApi
import com.fpt.edu.healthtracking.data.model.GraphData
import com.fpt.edu.healthtracking.data.model.WeightDTO
import com.fpt.edu.healthtracking.data.repository.GoalRepository
import com.fpt.edu.healthtracking.databinding.FragmentWeightGoalBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout

class ProgressFragment : BaseFragment<ProgressViewModel, FragmentWeightGoalBinding, GoalRepository>() {

    private lateinit var lineChart: LineChart
    private lateinit var goalData: GraphData
    private lateinit var weightLogAdapter: WeightLogAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupChart()
        setupRecyclerView()
        setupSpinner()
        observeViewModel()
        
        viewModel.getWeightData()
    }

    private fun setupViews() {
        lineChart = binding.lineChart
        
        binding.btnback.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.time_range_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.spinnerTimeRange.adapter = adapter
        binding.spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (::goalData.isInitialized) {
                    val timeRange = when(position) {
                        0 -> "1_week"
                        1 -> "1_month" 
                        2 -> "3_months"
                        3 -> "6_months"
                        4 -> "1_year"
                        else -> "1_week"
                    }
                    prepareGraphData(goalData.currentWeight, goalData.goalWeight, timeRange)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        weightLogAdapter = WeightLogAdapter()

        binding.rvWeightLog.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weightLogAdapter
        }
    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setDrawBorders(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                granularity = 1f
                labelRotationAngle = 0f
                valueFormatter = object : ValueFormatter() {
                    private val dateFormatter = DateTimeFormatter.ofPattern("dd/M")

                    override fun getFormattedValue(value: Float): String {
                        try {
                            val date = LocalDate.ofEpochDay(value.toLong())
                            return date.format(dateFormatter)
                        } catch (e: Exception) {
                            return ""
                        }
                    }
                }
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawAxisLine(true)
                axisMinimum = 0f
                granularity = 1f
            }
            
            axisRight.isEnabled = false
            
            animateX(1000)
            setScaleEnabled(true)
            setPinchZoom(true)
            
            marker = CustomMarkerView(context, R.layout.marker_view)
            
            legend.apply {
                form = Legend.LegendForm.LINE
                textSize = 12f
                textColor = ContextCompat.getColor(context, R.color.black)
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.goalData.observe(viewLifecycleOwner) { data ->
            data?.let {
                goalData = it
                prepareGraphData(goalData.currentWeight, goalData.goalWeight, "1_week")
                weightLogAdapter.submitList(it.currentWeight)
            } ?: run {
                Toast.makeText(requireContext(), "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareGraphData(currentWeight: List<WeightDTO>, goalWeight: List<WeightDTO>, timeRange: String) {
        val filteredCurrentWeight = filterDataByTimeRange(currentWeight, timeRange)
        val filteredGoalWeight = filterDataByTimeRange(goalWeight, timeRange)

        val dateRange = getDateRange(timeRange, filteredCurrentWeight)
        val entries = createEntries(dateRange, filteredCurrentWeight, filteredGoalWeight)
        
        updateChart(entries.first, entries.second, dateRange)
    }

    private fun getDateRange(timeRange: String, data: List<WeightDTO>): Pair<LocalDate, LocalDate> {
        val endDate = LocalDate.now()
        val startDate = when (timeRange) {
            "1_week" -> endDate.minusDays(7)
            "1_month" -> endDate.minusMonths(1)
            "3_months" -> endDate.minusMonths(3)
            "6_months" -> endDate.minusMonths(6)
            "1_year" -> endDate.minusYears(1)
            else -> endDate.minusDays(7)
        }
        return Pair(startDate, endDate)
    }

    private fun createEntries(
        dateRange: Pair<LocalDate, LocalDate>,
        currentWeight: List<WeightDTO>,
        goalWeight: List<WeightDTO>
    ): Pair<List<Entry>, List<Entry>> {
        val currentEntries = mutableListOf<Entry>()
        val goalEntries = mutableListOf<Entry>()
        
        val sortedCurrentWeight = currentWeight
            .filter { weight ->
                val date = parseDate(weight.date)
                !date.isBefore(dateRange.first) && !date.isAfter(dateRange.second)
            }
            .sortedBy { parseDate(it.date) }
        
        val sortedGoalWeight = goalWeight
            .filter { weight ->
                val date = parseDate(weight.date)
                !date.isBefore(dateRange.first) && !date.isAfter(dateRange.second)
            }
            .sortedBy { parseDate(it.date) }

        if (sortedCurrentWeight.isNotEmpty()) {
            val firstDataDate = parseDate(sortedCurrentWeight.first().date)
            if (dateRange.first.isBefore(firstDataDate)) {
                currentEntries.add(Entry(
                    dateRange.first.toEpochDay().toFloat(),
                    sortedCurrentWeight.first().weight.toFloat()
                ))
            }

            var lastWeight = sortedCurrentWeight.first().weight.toFloat()
            sortedCurrentWeight.forEach { weight ->
                val date = parseDate(weight.date)
                val epochDay = date.toEpochDay()
                lastWeight = weight.weight.toFloat()
                currentEntries.add(Entry(epochDay.toFloat(), lastWeight))
            }

            val lastDataDate = parseDate(sortedCurrentWeight.last().date)
            if (dateRange.second.isAfter(lastDataDate)) {
                currentEntries.add(Entry(
                    dateRange.second.toEpochDay().toFloat(),
                    lastWeight
                ))
            }
        }

        if (sortedGoalWeight.isNotEmpty()) {
            val firstDataDate = parseDate(sortedGoalWeight.first().date)
            if (dateRange.first.isBefore(firstDataDate)) {
                goalEntries.add(Entry(
                    dateRange.first.toEpochDay().toFloat(),
                    sortedGoalWeight.first().weight.toFloat()
                ))
            }

            var lastWeight = sortedGoalWeight.first().weight.toFloat()
            sortedGoalWeight.forEach { weight ->
                val date = parseDate(weight.date)
                val epochDay = date.toEpochDay()
                lastWeight = weight.weight.toFloat()
                goalEntries.add(Entry(epochDay.toFloat(), lastWeight))
            }

            val lastDataDate = parseDate(sortedGoalWeight.last().date)
            if (dateRange.second.isAfter(lastDataDate)) {
                goalEntries.add(Entry(
                    dateRange.second.toEpochDay().toFloat(),
                    lastWeight
                ))
            }
        }
        
        return Pair(currentEntries, goalEntries)
    }

    private fun findNearestWeight(date: LocalDate, weights: List<WeightDTO>): Float? {
        if (weights.isEmpty()) return null
        
        // Tìm giá trị gần nhất trước date
        val nearestBefore = weights
            .filter { parseDate(it.date) <= date }
            .maxByOrNull { parseDate(it.date) }
            ?.weight?.toFloat()
        
        // Tìm giá trị gần nhất sau date
        val nearestAfter = weights
            .filter { parseDate(it.date) > date }
            .minByOrNull { parseDate(it.date) }
            ?.weight?.toFloat()
        
        return when {
            nearestBefore != null && nearestAfter != null -> {
                // Nếu có cả trước và sau, lấy giá trị gần hơn
                val daysToBefore = ChronoUnit.DAYS.between(
                    parseDate(weights.first { it.weight.toFloat() == nearestBefore }.date),
                    date
                ).absoluteValue
                val daysToAfter = ChronoUnit.DAYS.between(
                    date,
                    parseDate(weights.first { it.weight.toFloat() == nearestAfter }.date)
                ).absoluteValue
                if (daysToBefore <= daysToAfter) nearestBefore else nearestAfter
            }
            nearestBefore != null -> nearestBefore
            nearestAfter != null -> nearestAfter
            else -> null
        }
    }

    private fun updateChart(
        currentWeightEntries: List<Entry>,
        goalWeightEntries: List<Entry>,
        dateRange: Pair<LocalDate, LocalDate>
    ) {
        val allWeights = (currentWeightEntries + goalWeightEntries).map { it.y }
        val minWeight = allWeights.minOrNull() ?: 0f
        val maxWeight = allWeights.maxOrNull() ?: 100f

        val yMin = when {
            minWeight % 10 == 0f -> minWeight - 10
            else -> (minWeight / 10).toInt() * 10f
        }
        val yMax = when {
            maxWeight % 10 == 0f -> maxWeight + 10
            else -> ((maxWeight / 10).toInt() + 1) * 10f
        }


        val currentWeightDataSet = LineDataSet(currentWeightEntries, "Cân nặng hiện tại").apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue_700)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue_700))
            lineWidth = 2f
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
            
            setDrawCircles(false)
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleRadius = 2f
            
            circleColors = listOf(ContextCompat.getColor(requireContext(), R.color.blue_700))
            
            setDrawIcons(true)
            enableDashedLine(0f, 0f, 0f)
        }

        val goalWeightDataSet = LineDataSet(goalWeightEntries, "Mục tiêu").apply {
            color = ContextCompat.getColor(requireContext(), R.color.green)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.green))
            lineWidth = 2f
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
            
            // Cấu hình node
            setDrawCircles(false)
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleRadius = 2f
            
            // Màu sắc
            circleColors = listOf(ContextCompat.getColor(requireContext(), R.color.green))
            
            setDrawIcons(true)
            enableDashedLine(0f, 0f, 0f)
        }

        lineChart.data = LineData(currentWeightDataSet, goalWeightDataSet)
        
        // Cấu hình trục Y
        lineChart.axisLeft.apply {
            axisMinimum = yMin
            axisMaximum = yMax
            granularity = 10f
        }
        
        // Set visible range cho trục X
        val minX = dateRange.first.toEpochDay().toFloat()
        val maxX = dateRange.second.toEpochDay().toFloat()
        
        lineChart.xAxis.apply {
            axisMinimum = minX
            axisMaximum = maxX
        }
        
        lineChart.invalidate()
    }

    private fun filterDataByTimeRange(data: List<WeightDTO>, timeRange: String): List<WeightDTO> {
        val endDate = LocalDate.now()
        val startDate = when (timeRange) {
            "1_week" -> endDate.minus(1, ChronoUnit.WEEKS)
            "1_month" -> endDate.minus(1, ChronoUnit.MONTHS)
            "3_months" -> endDate.minus(3, ChronoUnit.MONTHS)
            "6_months" -> endDate.minus(6, ChronoUnit.MONTHS)
            "1_year" -> endDate.minus(1, ChronoUnit.YEARS)
            else -> data.minOfOrNull { parseDate(it.date) } ?: endDate.minus(1, ChronoUnit.WEEKS)
        }

        return data.filter {
            val date = parseDate(it.date)
            !date.isBefore(startDate) && !date.isAfter(endDate)
        }
    }

    private fun parseDate(date: String): LocalDate {
        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        Log.e("parseDate","Original date string: $date")
        Log.e("parseDate","Parsed date: $parsedDate")
        Log.e("parseDate","Epoch day: ${parsedDate.toEpochDay()}")
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    }

    override fun getViewModel() = ProgressViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWeightGoalBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = GoalRepository(
        remoteDataSource.buildApi(GoalApi::class.java),
        userPreferences
    )
}
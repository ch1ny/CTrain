<template>
	<div>
		<el-container>
			<el-main>
				<el-card>
					<div style="text-align: left;" slot="header">
						<span><b>{{from}}到{{to}}的火车票查询</b></span>
					</div>
					<el-divider><i class="iconfont icon-huochepiao" style="font-weight: bold; font-size: 20px"></i></el-divider>
					<el-card>
						<div style="text-align: left;">
							<span><b>直达方案</b></span>
						</div>
						<el-table v-loading="searching" :data="trainList" stripe :cell-style="cellStyle" :header-cell-style="()=>{return 'text-align: center';}">
							<el-table-column type="expand">
								<template slot-scope="scope">
									<el-table :data="formatRoute(scope.row.stations)" :header-cell-style="()=>{return 'text-align: center';}" :cell-style="highlightRoute">
										<el-table-column prop="index" label="停靠站序" width="100">
										</el-table-column>
										<el-table-column prop="station" label="到达车站">
										</el-table-column>
										<el-table-column prop="arrive" label="进站时间">
										</el-table-column>
										<el-table-column prop="leave" label="发车时间">
										</el-table-column>
										<el-table-column prop="stop" label="停留时间">
										</el-table-column>
										<el-table-column prop="distance" label="总公里数">
										</el-table-column>
										<el-table-column prop="days" label="发车天数">
										</el-table-column>
									</el-table>
								</template>
							</el-table-column>
							<el-table-column prop="train" label="列车车号" width="150"></el-table-column>
							<el-table-column label="出发车站">
								<template slot-scope="scope">
									<span style="font-size: 20px">{{scope.row.from}}站 </span><i class="el-icon-time"></i> <span style="font-weight: bold">{{scope.row.startTime}}</span>
								</template>
							</el-table-column>
							<el-table-column label="到达车站">
								<template slot-scope="scope">
									<span style="font-size: 20px">{{scope.row.to}}站 </span><i class="el-icon-time"></i> <span style="font-weight: bold">{{scope.row.endTime}}</span><sup style="color: red" v-if="scope.row.days!=0"> +{{scope.row.days}}</sup>
								</template>
							</el-table-column>
							<el-table-column prop="start" label="始发车站" width="150">
								<template slot-scope="scope">
									<span style="color: #aaa">{{scope.row.start}}站</span>
								</template>
							</el-table-column>
							<el-table-column prop="end" label="终点车站" width="150">
								<template slot-scope="scope">
									<span style="color: #aaa">{{scope.row.end}}站</span>
								</template>
							</el-table-column>
							<el-table-column label="操作">
								<template slot-scope="scope">
									<el-button type="primary" @click="chooseDate1=true; form1.train=scope.row.train; form1.from=scope.row.from; form1.to=scope.row.to;"><i class="el-icon-s-ticket"></i>购票</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-card>
					<el-divider><i class="iconfont icon-huochepiao" style="font-weight: bold; font-size: 20px"></i></el-divider>
					<el-card>
						<div style="text-align: left;">
							<span><b>换乘方案</b></span>
						</div>
						<el-table v-loading="searching" :data="transferList" stripe :cell-style="()=>{return 'text-align: center';}" :header-cell-style="()=>{return 'text-align: center';}">
							<el-table-column type="expand">
								<template slot-scope="scope">
									<el-card>
										<div style="text-align: left;">
											<span><b>{{ scope.row.fromTrain }}</b></span>
										</div>
										<el-table :data="formatRoute(scope.row.fromRoutes)" :header-cell-style="()=>{return 'text-align: center';}" :cell-style="highlightRoute">
											<el-table-column prop="index" label="停靠站序" width="80">
											</el-table-column>
											<el-table-column prop="station" label="到达车站">
											</el-table-column>
											<el-table-column prop="arrive" label="进站时间">
											</el-table-column>
											<el-table-column prop="leave" label="发车时间">
											</el-table-column>
											<el-table-column prop="stop" label="停留时间">
											</el-table-column>
											<el-table-column prop="distance" label="总公里数">
											</el-table-column>
											<el-table-column prop="days" label="发车天数">
											</el-table-column>
										</el-table>
									</el-card>
									<el-divider>换 <i class="iconfont icon-huochepiao" style="font-weight: bold; font-size: 20px"></i> 乘</el-divider>
									<el-card>
										<div style="text-align: left;">
											<span><b>{{ scope.row.toTrain }}</b></span>
										</div>
										<el-table :data="formatRoute(scope.row.toRoutes)" :header-cell-style="()=>{return 'text-align: center';}" :cell-style="highlightRoute">
											<el-table-column prop="index" label="停靠站序" width="80">
											</el-table-column>
											<el-table-column prop="station" label="到达车站">
											</el-table-column>
											<el-table-column prop="arrive" label="进站时间">
											</el-table-column>
											<el-table-column prop="leave" label="发车时间">
											</el-table-column>
											<el-table-column prop="stop" label="停留时间">
											</el-table-column>
											<el-table-column prop="distance" label="总公里数">
											</el-table-column>
											<el-table-column prop="days" label="发车天数">
											</el-table-column>
										</el-table>
									</el-card>
								</template>
							</el-table-column>
							<el-table-column label="列车车次" width="150">
								<template slot-scope="scope">
									<span style="font-weight: bold; font-size: 20px">{{scope.row.fromTrain}}</span>
								</template>
							</el-table-column>
							<el-table-column label="出发车站">
								<template slot-scope="scope">
									<span style="font-size: 20px">{{scope.row.fromStation}}站 </span><i class="el-icon-time"></i> <span style="font-weight: bold">{{scope.row.startTime}}</span>
								</template>
							</el-table-column>
							<el-table-column label="换乘车站">
								<template slot-scope="scope">
									<div>
										<span style="font-size: 20px">{{scope.row.offStation}}站 </span><i class="el-icon-time"></i> <span style="font-weight: bold">{{scope.row.offTime}}</span><sup style="color: red" v-if="scope.row.fromDays!=0"> +{{scope.row.fromDays}}</sup>
									</div>
									<div>
										<span style="font-size: 20px">{{scope.row.onStation}}站 </span><i class="el-icon-time"></i> <span style="font-weight: bold">{{scope.row.onTime}}</span><sup style="color: red" v-if="(scope.row.onTime)>(scope.row.offTime)?scope.row.fromDays:scope.row.fromDays+1!=0"> +{{(scope.row.onTime)>(scope.row.offTime)?scope.row.fromDays:scope.row.fromDays+1 }}</sup>
									</div>
								</template>
							</el-table-column>
							<el-table-column label="换乘车次" width="150">
								<template slot-scope="scope">
									<span style="font-weight: bold; font-size: 20px">{{scope.row.toTrain}}</span>
								</template>
							</el-table-column>
							<el-table-column label="终点车站">
								<template slot-scope="scope">
									<span style="font-size: 20px">{{scope.row.toStation}}站 </span><i class="el-icon-time"></i> <span style="font-weight: bold">{{scope.row.endTime}}</span><sup style="color: red" v-if="scope.row.days!=0"> +{{scope.row.days}}</sup>
								</template>
							</el-table-column>
							<el-table-column label="操作">
								<template slot-scope="scope">
									<el-button type="primary" @click="chooseDate2=true; form2.fromTrain=scope.row.fromTrain; form2.toTrain=scope.row.toTrain;form2.from=scope.row.fromStation; form2.to=scope.row.toStation; form2.off=scope.row.offStation; form2.on=scope.row.onStation; form2.days=(scope.row.onTime)>(scope.row.offTime)?scope.row.fromDays:scope.row.fromDays+1"><i class="el-icon-s-ticket"></i>购票</el-button>
								</template>
							</el-table-column>
						</el-table>
					</el-card>
				</el-card>
			</el-main>
		</el-container>

		<div>
			<el-dialog title="选择出行日期" :visible.sync="chooseDate1" top="30vh" @open="date=undefined">
				<div>
					<el-form>
						<el-form-item>
							<el-date-picker v-model="date" type="date" style="width: 50%" :picker-options="accessDate" value-format="yyyy-MM-dd" placeholder="请选择出发日期(仅限未来15天内)">
							</el-date-picker>
						</el-form-item>
					</el-form>
					<el-button type="primary" @click="getTicketsNum1" style="width: 30%" :disabled="date===undefined"><i class="el-icon-s-ticket"></i>查询余票</el-button>
				</div>
			</el-dialog>

			<el-dialog title="选择出行日期" :visible.sync="chooseDate2" top="30vh" @open="date=undefined">
				<div>
					<el-form>
						<el-form-item>
							<el-date-picker v-model="date" type="date" style="width: 50%" :picker-options="accessDate" value-format="yyyy-MM-dd" placeholder="请选择出发日期(仅限未来15天内)">
							</el-date-picker>
						</el-form-item>
					</el-form>
					<el-button type="primary" @click="getTicketsNum2" style="width: 30%" :disabled="date===undefined"><i class="el-icon-s-ticket"></i>查询余票</el-button>
				</div>
			</el-dialog>
		</div>
	</div>
</template>

<script>
import axios from "axios";
import $ajax from "../ajax";
export default {
	data() {
		return {
			from: "",
			to: "",
			trainList: [],
			transferList: [],
			chooseDate1: false,
			chooseDate2: false,
			form1: {
				train: "",
				from: "",
				to: "",
			},
			form2: {
				fromTrain: "",
				toTrain: "",
				from: "",
				off: "",
				on: "",
				to: "",
				days: 0,
			},
			date: "",
			accessDate: {
				disabledDate(time) {
					return (
						time.getTime() < Date.now() ||
						time.getTime() > Date.now() + 1000 * 3600 * 24 * 15
					);
				},
			},
			searching: true,
		};
	},
	methods: {
		async search() {
			let res = await $ajax.post(
				"/service/search",
				{
					start: this.from,
					dest: this.to,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.trainList = res.data.data;
			}
			return res;
		},
		async searchTransfer() {
			let res = await $ajax.post(
				"/service/searchTransfer",
				{
					start: this.from,
					dest: this.to,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.transferList = res.data.data;
			}
			return res;
		},
		getTicketsNum1() {
			this.$router.push({
				path: "/tickets",
				query: {
					train: this.form1.train,
					from: this.form1.from,
					to: this.form1.to,
					date: this.date,
				},
			});
		},
		getTicketsNum2() {
			this.$router.push({
				path: "/transferTickets",
				query: {
					fromTrain: this.form2.fromTrain,
					toTrain: this.form2.toTrain,
					from: this.form2.from,
					off: this.form2.off,
					on: this.form2.on,
					to: this.form2.to,
					days: this.form2.days,
					date: this.date,
				},
			});
		},
		cellStyle({ columnIndex }) {
			switch (columnIndex) {
				case 1:
				case 4:
				case 5:
					return "font-weight: bolder; text-align: center; font-size: 20px";
				default:
					return "text-align: center";
			}
		},
		formatRoute(stations) {
			let routes = [];
			for (let key in stations) {
				let station = JSON.parse(JSON.stringify(stations[key]));
				station.station = station.station + "站";
				let arrive = station.arrive;
				let stop = station.stop;
				let hour = Number(arrive.split(":")[0]);
				let minute = Number(arrive.split(":")[1]) + Number(stop);
				if (minute >= 60) {
					minute -= 60;
					hour += 1;
					if (hour >= 24) {
						hour -= 24;
					}
				}
				if (minute < 10) {
					minute = "0" + minute;
				}
				if (hour < 10) {
					hour = "0" + hour;
				}
				let leave = hour + ":" + minute;
				station.leave = leave;
				routes.push(station);
			}
			return routes;
		},
		highlightRoute({ row }) {
			if (!row.access) {
				return "color: 	#aaaaaa; text-align: center";
			}
			return "font-weight: bolder; text-align: center";
		},
	},
	created() {
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
		this.from = this.$route.query.from;
		this.to = this.$route.query.to;
		document.title = `${this.from}到${this.to}的火车票查询`;
		// this.search();
		// this.searchTransfer();
		this.searching = true;
		axios.all([this.search(), this.searchTransfer()]).then(
			axios.spread((...ress) => {
				// ress.forEach((val) => {
				// 	console.log(val);
				// });
				if (ress.length == 2) {
					this.searching = false;
				}
			})
		);
	},
	watch: {
		$route(to) {
			this.from = to.query.from;
			this.to = to.query.to;
			document.title = `${this.from}到${this.to}的火车票查询`;
			this.search();
			this.searchTransfer();
		},
	},
};
</script>
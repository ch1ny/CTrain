<template>
	<div>
		<el-container>
			<el-main>
				<el-card>
					<div style="text-align: left;" slot="header">
						<span><b>{{train}}次列车{{from}}站到{{to}}站余票查询</b></span>
					</div>
					<el-table v-loading="loading" :data="tickets" stripe border :cell-style="()=>{return 'text-align: center';}" :header-cell-style="()=>{return 'text-align: center';}">
						<el-table-column label="座位类型">
							<template slot-scope="scope">
								<span>{{scope.row.type}}</span>
							</template>
						</el-table-column>
						<el-table-column label="剩余票数">
							<template slot-scope="scope">
								<span>余 <b :style="{'color': scope.row.num>=100?'mediumseagreen':scope.row.num >= 30?'orange':'red'}">{{scope.row.num}}</b> 张</span>
							</template>
						</el-table-column>
						<el-table-column label="预计票价">
							<template slot-scope="scope">
								<span>{{scope.row.price}}元</span>
							</template>
						</el-table-column>
						<el-table-column label="操作">
							<template slot-scope="scope">
								<el-button type="primary" @click="openPurchaser(scope.row)"><i class="el-icon-s-ticket"></i>购票</el-button>
							</template>
						</el-table-column>
					</el-table>
				</el-card>
			</el-main>
		</el-container>

		<div>
			<el-dialog title="乘客信息" :visible.sync="purchasing" :append-to-body="true" @open="passengers=[]">
				<el-table :data="passengers">
					<el-table-column prop="name" label="姓名"></el-table-column>
					<el-table-column prop="id" label="身份证号"></el-table-column>
					<el-table-column label="操作">
						<template slot-scope="scope">
							<el-button type="danger" @click="passengers.splice(scope.$index, 1)">移除</el-button>
						</template>
					</el-table-column>
				</el-table>
				<div style="margin-top: 10px">
					<el-button plain @click="showFriends=true;" style="width: 20%">添加乘车人</el-button>
					<el-button type="primary" @click="purchase" style="width: 20%" :disabled="passengers.length==0">确定购票</el-button>
				</div>
			</el-dialog>

			<el-dialog title="添加乘车人" :visible.sync="showFriends" :append-to-body="true">
				<el-table :data="getAccessFriends">
					<el-table-column prop="name" label="姓名"></el-table-column>
					<el-table-column prop="id" label="身份证号"></el-table-column>
					<el-table-column label="操作">
						<template slot-scope="scope">
							<el-button type="primary" @click="passengers.push(getAccessFriends[scope.$index]); showFriends=false;">选择</el-button>
						</template>
					</el-table-column>
					<div slot="append">
						<el-button @click="isAdding=true;" plain style="width: 100%; margin-bottom: 1px">新增乘车人</el-button>
					</div>
				</el-table>
			</el-dialog>

			<el-dialog title="新增乘车人" :visible.sync="isAdding" :append-to-body="true">
				<el-form :model="addForm" ref="addForm" :rules="addRules">
					<el-form-item prop="id">
						<el-input v-model="addForm.id" placeholder="请输入同行人员的身份证号码" prefix-icon="iconfont icon-credentials_icon"></el-input>
					</el-form-item>
					<el-form-item prop="name">
						<el-input v-model="addForm.name" placeholder="请输入同行人员的姓名" prefix-icon="el-icon-user"></el-input>
					</el-form-item>
				</el-form>
				<el-button type="primary" style="width: 15%" @click="add">添&nbsp;&nbsp;&nbsp;加</el-button>
			</el-dialog>
		</div>
	</div>
</template>

<script>
import $ajax from "../ajax";
export default {
	data() {
		return {
			train: "",
			from: "",
			to: "",
			date: "",
			tickets: [],
			friends: [],
			purchasing: false,
			passengers: [],
			showFriends: false,
			isAdding: false,
			addFriend: false,
			addForm: {
				id: "",
				name: "",
			},
			addRules: {
				id: [
					{
						required: true,
						message: "请输入同行人员的身份证号码",
						trigger: ["blur", "change"],
					},
				],
				name: [
					{
						required: true,
						message: "请输入同行人员的姓名",
						trigger: ["blur", "change"],
					},
				],
			},
			seat: "",
			price: "",
			loading: true,
		};
	},
	methods: {
		async getTickets() {
			this.loading = true;
			let res = await $ajax.post(
				"/service/getTickets",
				{
					train: this.train,
					from: this.from,
					to: this.to,
					date: this.date,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.tickets = res.data.data;
			}
			this.loading = false;
		},
		async getFriends() {
			let res = await $ajax.post(
				"/friend/get",
				{},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			this.friends = res.data.data;
		},
		add() {
			this.$refs.addForm.validate(async (valid) => {
				if (valid) {
					let res = await $ajax.post(
						"/friend/add",
						{
							id: this.addForm.id,
							name: this.addForm.name,
						},
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					if (res.data.code == 0) {
						let json = JSON.parse(JSON.stringify(this.addForm));
						this.friends.push(json);
						this.$message({
							message: "添加成功！",
							type: "success",
							duration: 1000,
						});
						this.isAdding = false;
					} else {
						this.$message({
							message: `添加失败！${res.data.data}`,
							type: "error",
							duration: 1000,
						});
					}
				}
			});
		},
		openPurchaser(row) {
			this.purchasing = true;
			this.seat = row.type;
			this.price = row.price;
		},
		async purchase() {
			let indents = [];
			indents.push({
				train: this.train,
				from: this.from,
				to: this.to,
				date: this.date,
				seat: this.seat,
				price: this.price,
				passengers: JSON.stringify(this.passengers),
			});
			let res = await $ajax.post(
				"/service/purchase",
				{
					indents: JSON.stringify(indents),
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.$notify({
					message: "购票成功！",
					type: "success",
					duration: 1000,
					onClose: () => {
						this.$router.push({ path: "/indent" });
					},
				});
			} else {
				this.$notify({
					message: `购票失败！${res.data.data}`,
					type: "error",
					duration: 1000,
				});
			}
		},
	},
	created() {
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
		this.train = this.$route.query.train;
		this.from = this.$route.query.from;
		this.to = this.$route.query.to;
		this.date = this.$route.query.date;
		this.getTickets();
		this.getFriends();
	},
	computed: {
		getAccessFriends() {
			return this.friends.filter((i) => {
				return this.passengers.indexOf(i) == -1;
			});
		},
	},
};
</script>